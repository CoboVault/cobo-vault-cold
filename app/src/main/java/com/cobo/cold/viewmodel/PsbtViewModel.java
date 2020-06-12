/*
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cobo.cold.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.Util;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.MainApplication;
import com.cobo.cold.Utilities;
import com.cobo.cold.callables.GetMasterFingerprintCallable;
import com.cobo.cold.update.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cobo.cold.viewmodel.GlobalViewModel.getAccount;


public class PsbtViewModel extends AndroidViewModel {

    public static final String WASABI_SIGN_ID = "wasabi_sign_id";
    public static final String BLUE_WALLET_SIGN_ID = "blue_wallet_sign_id";
    public static final String GENERIC_WALLET_SIGN_ID = "generic_wallet_sign_id";
    private static Pattern signedTxnPattern = Pattern.compile("^signed_[0-9a-fA-F]{8}.psbt$");
    private Storage storage;

    public PsbtViewModel(@NonNull Application application) {
        super(application);
        storage = Storage.createByEnvironment(application);
    }

    public static JSONObject adapt(JSONObject psbt) throws JSONException, WatchWalletNotMatchException {
        JSONObject object = new JSONObject();
        JSONArray inputs = new JSONArray();
        JSONArray outputs = new JSONArray();
        adaptInputs(psbt.getJSONArray("inputs"), inputs);
        if (inputs.length() < 1) {
            throw new WatchWalletNotMatchException("no input match masterFingerprint");
        }

        adaptOutputs(psbt.getJSONArray("outputs"), outputs);
        object.put("inputs", inputs);
        object.put("outputs", outputs);
        return object;
    }

    private static void adaptInputs(JSONArray psbtInputs, JSONArray inputs) throws JSONException {
        String masterKeyFingerprint = new GetMasterFingerprintCallable().call();
        Coins.Account account = getAccount(MainApplication.getApplication());

        for (int i = 0; i < psbtInputs.length(); i++) {
            JSONObject psbtInput = psbtInputs.getJSONObject(i);
            JSONObject in = new JSONObject();
            JSONObject utxo = new JSONObject();
            in.put("hash", psbtInput.getString("txId"));
            in.put("index", psbtInput.getInt("index"));
            JSONArray bip32Derivation = psbtInput.getJSONArray("hdPath");
            for (int j = 0; j < bip32Derivation.length(); j++) {
                JSONObject item = bip32Derivation.getJSONObject(j);
                String hdPath = item.getString("path");
                if (item.getString("masterFingerprint").equals(masterKeyFingerprint)
                    && hdPath.toUpperCase().startsWith(account.getPath())) {
                    utxo.put("publicKey", item.getString("pubkey"));
                    utxo.put("value", psbtInput.optInt("value"));
                    in.put("utxo", utxo);
                    in.put("ownerKeyPath", hdPath);
                    in.put("masterFingerprint", item.getString("masterFingerprint"));
                    inputs.put(in);
                    break;
                }
            }

        }

    }

    private static void adaptOutputs(JSONArray psbtOutputs, JSONArray outputs) throws JSONException {
        for(int i = 0; i < psbtOutputs.length(); i++) {
            JSONObject psbtOutput = psbtOutputs.getJSONObject(i);
            JSONObject out = new JSONObject();
            out.put("address", psbtOutput.getString("address"));
            out.put("value", psbtOutput.getInt("value"));
            outputs.put(out);
        }
    }

    private boolean isSignedPsbt(String fileName) {
        Matcher matcher = signedTxnPattern.matcher(fileName);
        return matcher.matches();
    }

    public LiveData<List<String>> loadUnsignPsbt() {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<String> fileList = new ArrayList<>();
            if (storage != null) {
                File[] files = storage.getElectrumDir().listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".psbt")
                                && !isSignedPsbt(f.getName())) {
                            fileList.add(f.getName());
                        }
                    }
                }
            }
            result.postValue(fileList);
        });
        return result;
    }
}
