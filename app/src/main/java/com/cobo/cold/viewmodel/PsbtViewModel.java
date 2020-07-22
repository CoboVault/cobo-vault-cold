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

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.MainApplication;
import com.cobo.cold.callables.GetMasterFingerprintCallable;
import com.cobo.cold.update.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cobo.cold.viewmodel.GlobalViewModel.getAccount;


public class PsbtViewModel extends AndroidViewModel {
    private static Pattern signedTxnPattern = Pattern.compile("^signed_[0-9a-fA-F]{8}.psbt$");
    private Storage storage;

    public PsbtViewModel(@NonNull Application application) {
        super(application);
        storage = Storage.createByEnvironment(application);
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
