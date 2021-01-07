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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.coins.ETH.EthImpl;
import com.cobo.coinlib.coins.SignTxResult;
import com.cobo.coinlib.exception.InvalidPathException;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.Signer;
import com.cobo.coinlib.path.CoinPath;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.db.entity.AddressEntity;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.encryption.ChipSigner;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class EthTxConfirmViewModel extends TxConfirmViewModel {
    private final MutableLiveData<Boolean> addingAddress = new MutableLiveData<>();
    private String hdPath;
    private String signId;
    private String txHex;
    private int chainId;
    private JSONObject abi;
    private String txId;
    public EthTxConfirmViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<TxEntity> getObservableTx() {
        return observableTx;
    }

    public MutableLiveData<Exception> parseTxException() {
        return parseTxException;
    }

    public void parseTxData(JSONObject object) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                Log.i(TAG, "object = " + object.toString(4));
                hdPath = object.getString("hdPath");
                signId = object.getString("signId");
                txHex = object.getString("txHex");
                JSONObject ethTx = EthImpl.decodeRawTransaction(txHex);
                if (ethTx == null) {
                    observableTx.postValue(null);
                    parseTxException.postValue(new InvalidTransactionException("invalid transaction"));
                    return;
                }
                chainId = ethTx.getInt("chainId");
                String data = ethTx.getString("data");
                try {
                    abi = new JSONObject(data);
                } catch (JSONException ignore) { }
                TxEntity tx = generateTxEntity(ethTx);
                observableTx.postValue(tx);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private TxEntity generateTxEntity(JSONObject object) throws JSONException {
        TxEntity tx = new TxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        coinCode = Coins.ETH.coinCode();
        tx.setSignId(WatchWallet.METAMASK_SIGN_ID);
        tx.setTimeStamp(getUniversalSignIndex(getApplication()));
        tx.setCoinCode(coinCode);
        tx.setCoinId(Coins.ETH.coinId());
        tx.setFrom(getFromAddress(hdPath));
        tx.setTo(object.getString("to"));
        BigDecimal amount = new BigDecimal(object.getString("value"));
        double value = amount.divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
        tx.setAmount(nf.format(value) + " ETH");
        tx.setFee(nf.format(calculateDisplayFee(object)) + " ETH");
        tx.setMemo(object.getString("data"));
        tx.setBelongTo(mRepository.getBelongTo());
        return tx;
    }

    private double calculateDisplayFee(JSONObject ethTx) throws JSONException {
        BigDecimal gasPrice = new BigDecimal(ethTx.getString("gasPrice"));
        BigDecimal gasLimit = new BigDecimal(ethTx.getString("gasLimit"));
        return gasLimit.multiply(gasPrice)
                .divide(BigDecimal.TEN.pow(18), 8, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public String getFromAddress(String path) {
        ensureAddressExist(path);
        return mRepository.loadAddressBypath(path).getAddressString();
    }

    private void ensureAddressExist(String path) {
        AddressEntity address = mRepository.loadAddressBypath(path);
        if (address == null) {
            addAddress(getAddressIndex(path));
        }
    }

    private void addAddress(int addressIndex) {
        CoinEntity coin = mRepository.loadCoinSync(Coins.coinIdFromCoinCode(coinCode));
        int addressLength = mRepository.loadAccountsForCoin(coin).get(0).getAddressLength();

        if (addressLength < addressIndex + 1) {
            String[] names = new String[addressIndex + 1 - addressLength];
            int index = 0;
            for (int i = addressLength; i < addressIndex + 1; i++) {
                names[index++] = coinCode + "-" + i;
            }
            final CountDownLatch mLatch = new CountDownLatch(1);
            addingAddress.postValue(true);
            new AddAddressViewModel.AddAddressTask(coin, mRepository, mLatch::countDown)
                    .execute(names);
            try {
                mLatch.await();
                addingAddress.postValue(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int getAddressIndex(String hdPath) {
        try {
            return CoinPath.parsePath(hdPath).getValue();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignCallback();
            callback.startSign();
            Signer signer = initSigner();
            signTransaction(callback, signer);
        });
    }

    protected TxEntity onSignSuccess(String txId, String rawTx) {
        TxEntity tx = observableTx.getValue();
        this.txId = txId;
        Objects.requireNonNull(tx).setTxId(txId);
        JSONObject signed = new JSONObject();
        try {
            signed.put("signature", EthImpl.getSignature(rawTx));
            signed.put("signId", signId);
            signed.put("chainId", chainId);
            signed.put("abi", abi);
            tx.setSignedHex(signed.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRepository.insertTx(tx);
        return tx;
    }

    private void signTransaction(@NonNull SignCallback callback, Signer signer) {
        if (signer == null) {
            callback.onFail();
            return;
        }
        SignTxResult result = new EthImpl(chainId).signHex(txHex, signer);
        if (result == null) {
            callback.onFail();
        } else {
            callback.onSuccess(result.txId, result.txHex);
        }
    }

    private Signer initSigner() {
        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG,"authToken null");
            return null;
        }
        return new ChipSigner(hdPath.toLowerCase(), authToken);
    }

    public String getTxId() {
        return txId;
    }

    public JSONObject getAbi() {
        return abi;
    }

    public String getTxHex() {
        return Objects.requireNonNull(observableTx.getValue()).getSignedHex();
    }

    public int getChainId() {
        return chainId;
    }
}
