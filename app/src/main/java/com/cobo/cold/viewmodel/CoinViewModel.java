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

import com.cobo.coinlib.exception.InvalidPathException;
import com.cobo.coinlib.path.AddressIndex;
import com.cobo.coinlib.path.CoinPath;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.Utilities;
import com.cobo.cold.db.entity.AddressEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CoinViewModel extends AndroidViewModel {

    private final DataRepository mRepository;

    public CoinViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication)application).getRepository();
    }

    public List<AddressEntity> filterChangeAddress(List<AddressEntity> addressEntities) {
        return addressEntities.stream()
                .filter(this::isChangeAddress)
                .collect(Collectors.toList());
    }

    public List<AddressEntity> filterReceiveAddress(List<AddressEntity> addressEntities) {
        return addressEntities.stream()
                .filter(addressEntity -> !isChangeAddress(addressEntity))
                .collect(Collectors.toList());
    }

    public List<AddressEntity> filterByAccountHdPath(List<AddressEntity> addressEntities, String hdPath) {
        return addressEntities.stream()
                .filter(addressEntity -> addressEntity.getPath().toUpperCase().startsWith(hdPath))
                .collect(Collectors.toList());
    }

    private boolean isChangeAddress(AddressEntity addressEntity) {
        String path = addressEntity.getPath();
        try {
            AddressIndex addressIndex = CoinPath.parsePath(path);
            return !addressIndex.getParent().isExternal();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return false;
    }

    public LiveData<List<AddressEntity>> getAddress() {
        return mRepository.loadAllAddress();
    }

    public void updateAddress(AddressEntity addr) {
        AppExecutors.getInstance().diskIO().execute(() -> mRepository.updateAddress(addr));
    }
}
