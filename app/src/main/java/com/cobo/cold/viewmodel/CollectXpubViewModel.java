/*
 *
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
 *
 */

package com.cobo.cold.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cobo.cold.AppExecutors;
import com.cobo.cold.update.utils.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class CollectXpubViewModel extends AndroidViewModel {

    private static Pattern xpubFileName = Pattern.compile("(.*)[0-9a-fA-F]{8}(.*).json$");

    private List<XpubInfo> xpubInfos;
    public boolean startCollect;
    private Storage storage;

    public CollectXpubViewModel(@NonNull Application application) {
        super(application);
        storage = Storage.createByEnvironment(application);
    }


    public void initXpubInfo(int total) {
        xpubInfos = new ArrayList<>(total);
    }

    public List<XpubInfo> getXpubInfo() {
        return xpubInfos;
    }


    public static class XpubInfo {
        public int index;
        public String xfp;
        public String xpub;

        public XpubInfo(int index, String fingerprint, String xpub) {
            this.index = index;
            this.xfp = fingerprint;
            this.xpub = xpub;
        }
    }

    public LiveData<List<File>> loadXpubFile() {
        MutableLiveData<List<File>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<File> fileList = new ArrayList<>();
            if (storage != null) {
                File[] files = storage.getElectrumDir().listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (xpubFileName.matcher(f.getName()).matches()
                                && !f.getName().startsWith(".")) {
                            fileList.add(f);
                        }
                    }
                }
            }
            fileList.sort((o1, o2) -> o1.getName().compareTo(o2.getName()));
            result.postValue(fileList);
        });
        return result;
    }
}
