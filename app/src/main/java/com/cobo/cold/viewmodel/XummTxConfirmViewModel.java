/*
 *
 *  Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cobo.cold.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.coins.XRP.Xrp;
import com.cobo.coinlib.coins.XRP.XrpImpl;
import com.cobo.coinlib.coins.XRP.xumm.SupportTransactions;
import com.cobo.coinlib.coins.XRP.xumm.XrpTransaction;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.Signer;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.db.entity.AddressEntity;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.encryption.ChipSigner;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Objects;

public class XummTxConfirmViewModel extends TxConfirmViewModel{

    private JSONObject xummTxObj;
    private String account;
    private String signingPubKey;
    private String signingKeyPath;

    private final MutableLiveData<JSONObject> displayJson = new MutableLiveData<>();
    public XummTxConfirmViewModel(@NonNull Application application) {
        super(application);
        coinCode = Coins.XRP.coinCode();
    }

    public MutableLiveData<JSONObject> getDisplayJson() {
        return displayJson;
    }

    public void parseXummTxData(JSONObject object) {
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                XrpTransaction xrpTransaction = SupportTransactions.get(object.getString("TransactionType"));

                if (xrpTransaction == null || !xrpTransaction.isValid(object)) {
                    parseTxException.postValue(new InvalidTransactionException("invalid xrp exception"));
                }
                if (!checkAccount()) {
                    parseTxException.postValue(new InvalidTransactionException("invalid xrp account"));
                }
                displayJson.postValue(xrpTransaction.flatTransactionDetail(object));
                xummTxObj = object;
                TxEntity tx = new TxEntity();
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(20);
                tx.setCoinCode(coinCode);
                tx.setSignId(watchWallet.getSignId());
                tx.setCoinId(Coins.XRP.coinId());
                tx.setBelongTo(mRepository.getBelongTo());
                observableTx.postValue(tx);
            }  catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean checkAccount() {
        String account = xummTxObj.optString("Account");
        String signingPubKey = xummTxObj.optString("SigningPubKey");
        this.account = account;
        this.signingPubKey = signingPubKey;

        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(signingPubKey)) {
            return false;
        }

        if (!Xrp.encodeAccount(signingPubKey).equals(account)) {
            return false;
        }

        for (AddressEntity address : mRepository.loadAddressSync(Coins.XRP.coinId())) {
            if (address.getAddressString().equals(account)) {
                signingKeyPath = address.getPath().toLowerCase();
                return true;
            }
        }
        return false;
    }

    public void handleSignXummTransaction() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignCallback();
            callback.startSign();
            Signer signer = new ChipSigner(signingKeyPath, getAuthToken(), signingPubKey);
            signXummTransaction(xummTxObj, callback, signer);
        });
    }

    public LiveData<CoinEntity> loadXrpCoinEntity() {
        return mRepository.loadCoin(Coins.XRP.coinId());
    }

    @Override
    protected TxEntity onSignSuccess(String txId, String rawTx) {
        TxEntity tx = observableTx.getValue();
        Objects.requireNonNull(tx).setTxId(txId);
        try {
            xummTxObj.put("txHex", rawTx);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tx.setSignedHex(xummTxObj.toString());
        mRepository.insertTx(tx);
        return tx;
    }

    public void signXummTransaction(JSONObject txObj, SignCallback callback, Signer signer ) {
        new Xrp(new XrpImpl()).signTx(txObj, callback, signer);
    }
}
