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

import com.cobo.coinlib.coins.BTC.Electrum.ElectrumTx;
import com.cobo.coinlib.coins.BTC.Electrum.TransactionInput;
import com.cobo.coinlib.coins.BTC.Electrum.TransactionOutput;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.update.utils.FileUtils;
import com.cobo.cold.update.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ElectrumViewModel extends AndroidViewModel {

    public static final String ELECTRUM_SIGN_ID = "electrum_sign_id";

    private static Pattern signedTxnPattern = Pattern.compile("^signed_[0-9a-fA-F]{8}.txn$");
    private final DataRepository mRepo;
    private MutableLiveData<String> exPub = new MutableLiveData<>();
    private Storage storage;

    public ElectrumViewModel(@NonNull Application application) {
        super(application);
        mRepo = MainApplication.getApplication().getRepository();
        storage = Storage.createByEnvironment(application);
    }

    public static JSONObject adapt(ElectrumTx tx) throws JSONException {
        JSONObject object = new JSONObject();
        JSONArray inputs = new JSONArray();
        JSONArray outputs = new JSONArray();
        adaptInputs(tx, inputs);
        adaptOutputs(tx, outputs);
        object.put("inputs", inputs);
        object.put("outputs", outputs);
        object.put("locktime", tx.getLockTime());
        object.put("version", tx.getVersion());
        return object;
    }

    private static void adaptInputs(ElectrumTx tx, JSONArray inputs) throws JSONException {
        for (TransactionInput transactionInput : tx.getInputs()) {
            JSONObject in = new JSONObject();
            JSONObject utxo = new JSONObject();
            in.put("hash", transactionInput.preTxId);
            in.put("index", transactionInput.preTxIndex);
            in.put("sequence", transactionInput.sequence);
            utxo.put("publicKey", transactionInput.pubKey.pubkey);
            utxo.put("value", transactionInput.value.intValue());
            in.put("utxo", utxo);
            in.put("hash", transactionInput.preTxId);
            in.put("ownerKeyPath", transactionInput.pubKey.hdPath);
            inputs.put(in);

        }
    }

    private static void adaptOutputs(ElectrumTx tx, JSONArray outputs) throws JSONException {
        for (TransactionOutput transactionOutput : tx.getOutputs()) {
            JSONObject out = new JSONObject();
            out.put("address", transactionOutput.address);
            out.put("value", transactionOutput.value);
            outputs.put(out);
        }
    }

    private boolean isSignedTxn(String fileName) {
        Matcher matcher = signedTxnPattern.matcher(fileName);
        return matcher.matches();
    }

    public LiveData<List<String>> loadUnsignTxn() {
        MutableLiveData<List<String>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<String> fileList = new ArrayList<>();
            if (storage != null) {
                File[] files = storage.getElectrumDir().listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".txn")
                                && !isSignedTxn(f.getName())) {
                            fileList.add(f.getName());
                        }
                    }
                }
            }
            result.postValue(fileList);
        });
        return result;
    }

    public LiveData<String> parseTxnFile(String file) {
        MutableLiveData<String> txnHex = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            String content = FileUtils.readString(new File(storage.getExternalDir(), file));
            try {
                JSONObject object = new JSONObject(content);
                String hex = object.getString("hex");
                txnHex.postValue(hex);
            } catch (JSONException e) {
                e.printStackTrace();
                txnHex.postValue(null);
            }
        });
        return txnHex;
    }
}
