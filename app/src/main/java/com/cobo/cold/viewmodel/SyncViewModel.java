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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.Util;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.db.entity.AccountEntity;
import com.cobo.cold.db.entity.AddressEntity;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.protocol.EncodeConfig;
import com.cobo.cold.protocol.builder.SyncBuilder;

import java.util.List;

public class SyncViewModel extends AndroidViewModel {

    private final DataRepository mRepository;

    public SyncViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
    }


    public List<AccountEntity> loadAccountForCoin(CoinEntity coin) {
        return mRepository.loadAccountsForCoin(coin);
    }

    public LiveData<String> generateSyncCobo() {
        MutableLiveData<String> sync = new MutableLiveData<>();
        sync.setValue("");
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<CoinEntity> coinEntities = mRepository.loadCoinsSync();
            SyncBuilder syncBuilder = new SyncBuilder(EncodeConfig.DEFAULT);
            for (CoinEntity entity : coinEntities) {
                SyncBuilder.Coin coin = new SyncBuilder.Coin();
                coin.setActive(entity.isShow());
                coin.setCoinCode(entity.getCoinCode());
                List<AccountEntity> accounts = loadAccountForCoin(entity);
                for (AccountEntity accountEntity : accounts) {
                    SyncBuilder.Account account = new SyncBuilder.Account();
                    account.addressLength = accountEntity.getAddressLength();
                    account.hdPath = accountEntity.getHdPath();
                    account.xPub = accountEntity.getExPub();
                    if (TextUtils.isEmpty(account.xPub)) {
                        continue;
                    }
                    account.isMultiSign = false;
                    coin.addAccount(account);
                }
                if (coin.accounts.size() > 0) {
                    syncBuilder.addCoin(coin);
                }
            }
            if (syncBuilder.getCoinsCount() == 0) {
                sync.postValue("");
            } else {
                sync.postValue(syncBuilder.build());
            }

        });
        return sync;
    }

    public LiveData<XrpSyncData> generateSyncXumm(final int index) {
        MutableLiveData<XrpSyncData> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(()->{
            CoinEntity xrp = mRepository.loadCoinSync(Coins.XRP.coinId());
            String pubkey = Util.getPublicKeyHex(xrp.getExPub(), 0, index);
            for (AddressEntity addressEntity : mRepository.loadAddressSync(Coins.XRP.coinId())) {
                if (addressEntity.getIndex() == index) {
                    result.postValue(new XrpSyncData(addressEntity, pubkey));
                }
            }
        });
        return result;
    }

    public static class XrpSyncData {
        public AddressEntity addressEntity;
        public String pubkey;

        public XrpSyncData(AddressEntity addressEntity, String pubkey) {
            this.addressEntity = addressEntity;
            this.pubkey = pubkey;
        }
    }

    public LiveData<String> generateSyncPolkadotjs(String coinCode) {
        MutableLiveData<String> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(()->{
            AddressEntity addressEntity = mRepository.loadAddressSync(Coins.coinIdFromCoinCode(coinCode)).get(0);
            String prefix = "substrate";
            String address = addressEntity.getAddressString();
            String genesisHash = getGenesisHash(coinCode);
            String name = "Cobo-"+Coins.coinNameFromCoinCode(coinCode);
            result.postValue(prefix + ":" + address + ":" + genesisHash + ":" + name);

        });
        return result;
    }

    private String getGenesisHash(String coinCode) {
        switch (coinCode) {
            case "DOT":
                return "0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3";
            case "KSM":
                return "0xb0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe";
        }
        return "";
    }


}
