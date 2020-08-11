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
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.db.entity.AccountEntity;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.model.Coin;
import com.cobo.cold.protocol.EncodeConfig;
import com.cobo.cold.protocol.builder.SyncBuilder;

import java.util.List;

public class CoinListViewModel extends AndroidViewModel {

    private final DataRepository mRepository;
    private final MediatorLiveData<List<CoinEntity>> mObservableCoins;

    public CoinListViewModel(@NonNull Application application) {
        super(application);

        mObservableCoins = new MediatorLiveData<>();
        mObservableCoins.setValue(null);
        mRepository = ((MainApplication) application).getRepository();
        mObservableCoins.addSource(mRepository.loadCoins(), mObservableCoins::setValue);
    }

    public LiveData<List<CoinEntity>> getCoins() {
        return mRepository.loadCoins();
    }

    public void toggleCoin(Coin coin) {
        CoinEntity entity = new CoinEntity(coin);
        entity.setShow(!coin.isShow());
        mRepository.updateCoin(entity);
    }

    public LiveData<CoinEntity> loadCoin(int id) {
        return mRepository.loadCoin(id);
    }

    public LiveData<TxEntity> loadTx(String txId) {
        return mRepository.loadTx(txId);
    }

    public LiveData<List<TxEntity>> loadTxs(String coinId) {
        return mRepository.loadTxs(coinId);
    }

    private List<AccountEntity> loadAccountForCoin(CoinEntity coin) {
        return mRepository.loadAccountsForCoin(coin);
    }

    public LiveData<String> generateSync(List<CoinEntity> coinEntities) {
        MutableLiveData<String> sync = new MutableLiveData<>();
        sync.setValue("");
        AppExecutors.getInstance().diskIO().execute(() -> {
            SyncBuilder syncBuilder = new SyncBuilder(EncodeConfig.DEFAULT);
            for (CoinEntity entity : coinEntities) {
                SyncBuilder.Coin coin = new SyncBuilder.Coin();
                coin.setActive(entity.isShow());
                coin.setCoinCode(entity.getCoinCode());
                List<AccountEntity> accounts = loadAccountForCoin(entity);
                for (AccountEntity accountEntity : accounts) {
                    //only sync account M/49'/0'/0' to cobo vault mobile
                    if (accountEntity.getHdPath().equals(Coins.Account.P2SH.getPath())) {
                        SyncBuilder.Account account = new SyncBuilder.Account();
                        account.addressLength = accountEntity.getAddressLength();
                        account.hdPath = accountEntity.getHdPath();
                        account.xPub = accountEntity.getExPub();
                        account.isMultiSign = false;
                        coin.addAccount(account);
                    }
                }
                if (coin.accounts.isEmpty()) continue;
                syncBuilder.addCoin(coin);
            }
            if (syncBuilder.getCoinsCount() == 0) {
                sync.postValue("");
            } else {
                sync.postValue(syncBuilder.build());
            }

        });
        return sync;
    }
}
