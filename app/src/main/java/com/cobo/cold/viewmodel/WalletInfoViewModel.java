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
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.callables.GetMasterFingerprintCallable;
import com.cobo.cold.db.entity.AccountEntity;
import com.cobo.cold.db.entity.CoinEntity;

public class WalletInfoViewModel extends AndroidViewModel {
    private final MutableLiveData<String> fingerprint = new MutableLiveData<>("");
    private final MutableLiveData<String> xpub = new MutableLiveData<>("");

    public WalletInfoViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> getFingerprint() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String masterFingerprint = new GetMasterFingerprintCallable().call();
            fingerprint.postValue(masterFingerprint);
        });
        return fingerprint;
    }

    public MutableLiveData<String> getXpub(Coins.Account account) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DataRepository repo = ((MainApplication)getApplication()).getRepository();
            CoinEntity coinEntity = repo.loadCoinEntityByCoinCode(Coins.BTC.coinCode());
            AccountEntity accountEntity = repo.loadAccountsByPath(coinEntity.getId(), account.getPath());
            xpub.postValue(accountEntity.getExPub());
        });
        return xpub;
    }
}
