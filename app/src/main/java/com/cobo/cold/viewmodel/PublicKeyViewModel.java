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
import androidx.lifecycle.MediatorLiveData;

import com.cobo.coinlib.coins.EOS.Eos;
import com.cobo.coinlib.coins.IOST.Iost;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.db.entity.AccountEntity;
import com.cobo.cold.db.entity.AddressEntity;
import com.cobo.cold.db.entity.CoinEntity;

import java.util.List;

public class PublicKeyViewModel extends AndroidViewModel {

    private final DataRepository mRepo;
    private final MediatorLiveData<String> observablePubKey = new MediatorLiveData<>();

    public PublicKeyViewModel(@NonNull Application application) {
        super(application);
        mRepo = MainApplication.getApplication().getRepository();
        observablePubKey.setValue("");
    }

    public LiveData<String> calcPubKey(String coinId) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity coin = mRepo.loadCoinSync(coinId);
            List<AccountEntity> accouts = mRepo.loadAccountsForCoin(coin);
            String expub = accouts.get(0).getExPub();
            String pubKey = null;
            if ("eos".equals(coinId)) {
                pubKey = new Eos.Deriver().derive(expub, 0, 0);
            } else if ("iost".equals(coinId)) {
                pubKey = new Iost.Deriver().derive(expub);
            }
            observablePubKey.postValue(pubKey);

        });
        return observablePubKey;
    }

    public String getAddress(@NonNull String coinId) {
        List<AddressEntity> list = mRepo.loadAddressSync(coinId);
        if (list != null && list.size() > 0) {
            return list.get(0).getAddressString();
        }
        return null;
    }
}
