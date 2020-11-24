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
import android.util.Log;

import androidx.annotation.NonNull;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.UOS.UOSDecoder;
import com.cobo.coinlib.coins.polkadot.UOS.Result;
import com.cobo.coinlib.exception.InvalidUOSException;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.Signer;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.encryption.ChipSigner;

import org.spongycastle.util.encoders.Hex;

import java.util.Objects;

public class PolkadotJsTxConfirmViewModel extends TxConfirmViewModel {

    private byte[] signingPayload;

    public PolkadotJsTxConfirmViewModel(@NonNull Application application) {
        super(application);
    }

    public void parseTxData(String data) {
        try {
            Result result = UOSDecoder.decode(data, false);
            TxEntity tx = generateSubstrateTxEntity(result);
            observableTx.postValue(tx);
            signingPayload = result.getSigningPayload();
        } catch (InvalidUOSException ignored) {
        }
    }

    private TxEntity generateSubstrateTxEntity(Result result) {
        TxEntity tx = new TxEntity();
        coinCode = getCoinCode(result.getNetwork());
        tx.setSignId(WatchWallet.POLKADOT_JS_SIGN_ID);
        tx.setTimeStamp(getUniversalSignIndex(getApplication()));
        tx.setCoinCode(coinCode);
        tx.setCoinId(Coins.coinIdFromCoinCode(coinCode));
        tx.setFrom(result.getAccount());
        tx.setBelongTo(mRepository.getBelongTo());
        return tx;
    }

    private String getCoinCode(Network network) {
        if (network.name.equals("Polkadot")) {
            return Coins.DOT.coinCode();
        } else if (network.name.equals("Kusama")) {
            return Coins.KSM.coinCode();
        }
        return null;
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            SignCallback callback = initSignCallback();
            callback.startSign();
            String authToken = getAuthToken();
            if (TextUtils.isEmpty(authToken)) {
                Log.w(TAG, "authToken null");
                callback.onFail();
            }
            Signer signer = new ChipSigner(coinCode.equals(Coins.DOT.coinCode()) ?
                    Coins.DOT.getAccounts()[0] : Coins.KSM.getAccounts()[0], authToken);
            String signedHex = signer.sign(Hex.toHexString(signingPayload));
            if (!TextUtils.isEmpty(signedHex)) {
                callback.onSuccess(signedHex, signedHex);
            } else {
                callback.onFail();
            }
        });
    }

    @Override
    protected TxEntity onSignSuccess(String txId, String rawTx) {
        TxEntity tx = observableTx.getValue();
        Objects.requireNonNull(tx).setTxId(txId);
        tx.setSignedHex("01" + rawTx);
        mRepository.insertTx(tx);
        return tx;
    }
}
