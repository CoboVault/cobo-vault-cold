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

import com.cobo.coinlib.coins.polkadot.UOS.Extrinsic;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.UOS.Result;
import com.cobo.coinlib.coins.polkadot.UOS.UOSDecoder;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.balance.TransferParameter;
import com.cobo.coinlib.exception.InvalidUOSException;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.Signer;
import com.cobo.coinlib.utils.Arith;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.db.entity.AddressEntity;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.encryption.ChipSigner;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PolkadotJsTxConfirmViewModel extends TxConfirmViewModel {

    private byte[] signingPayload;
    private boolean isHash;
    private final DataRepository mRepo;
    private Extrinsic extrinsic;
    private byte[] accountPublicKey;
    private String txId;

    public PolkadotJsTxConfirmViewModel(@NonNull Application application) {
        super(application);
        mRepo = ((MainApplication)application).getRepository();
    }
    private JSONObject extrinsicObject;

    public void parseTxData(String data) {
        try {
            Result result = UOSDecoder.decode(data, false);
            isHash = result.isHash;
            extrinsic = result.extrinsic;
            accountPublicKey = result.getAccountPublicKey();
            extrinsicObject = extrinsic.palletParameter.toJSON();
            TxEntity tx = generateSubstrateTxEntity(result);
            observableTx.postValue(tx);
            signingPayload = result.getSigningPayload();
        } catch (InvalidUOSException | JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean isTransactionSupported(Parameter parameter) {
        if (parameter == null) return false;
        return  parameter.name.startsWith("balance.transfer")
                || parameter.name.startsWith("staking")
                || parameter.name.startsWith("utility.batch")
                || parameter.name.startsWith("session.setKeys")
                || parameter.name.startsWith("democracy")
                || parameter.name.equals("identity.setIdentity")
                || parameter.name.equals("proxy.addProxy")
                || parameter.name.startsWith("electionsPhragmen")
                || parameter.name.startsWith("treasury")
                || parameter.name.startsWith("society.bid");
    }

    public boolean isNetworkSupported(Network network) {
        return Network.supportedNetworks.contains(network);
    }

    public boolean isAccountMatch(String account) {
        Future<Boolean> future = Executors.newSingleThreadExecutor().submit(() -> {
            List<AddressEntity> allSubstrateAddress = new ArrayList<>();
            allSubstrateAddress.addAll(mRepo.loadAddressSync(Coins.KSM.coinId()));
            allSubstrateAddress.addAll(mRepo.loadAddressSync(Coins.DOT.coinId()));
            return allSubstrateAddress.stream().anyMatch(entity -> account.equals(entity.getAddressString()));
        });
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private TxEntity generateSubstrateTxEntity(Result result) {
        TxEntity tx = new TxEntity();
        coinCode = result.getNetwork().coinCode();
        tx.setSignId(WatchWallet.POLKADOT_JS_SIGN_ID);
        tx.setTimeStamp(getUniversalSignIndex(getApplication()));
        tx.setCoinCode(coinCode);
        tx.setCoinId(Coins.coinIdFromCoinCode(coinCode));
        tx.setFrom(result.getAccount());
        tx.setFee(result.getExtrinsic().getTip()+ " " + coinCode);
        tx.setSignedHex(extrinsicObject.toString());
        tx.setBelongTo(mRepository.getBelongTo());
        return tx;
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
            try {
                String txId = extrinsic.getTxId(accountPublicKey,Hex.decode(signedHex));
                if (!TextUtils.isEmpty(signedHex)) {
                    callback.onSuccess(txId, signedHex);
                } else {
                    callback.onFail();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public String getTxId() {
        return txId;
    }

    @Override
    protected TxEntity onSignSuccess(String txId, String rawTx) {
        this.txId = txId;
        TxEntity tx = observableTx.getValue();
        Objects.requireNonNull(tx).setTxId(txId);
        try {
            extrinsicObject.put("signedHex","01" + rawTx);
            tx.setSignedHex(extrinsicObject.toString());
            mRepository.insertTx(tx);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tx;
    }
}
