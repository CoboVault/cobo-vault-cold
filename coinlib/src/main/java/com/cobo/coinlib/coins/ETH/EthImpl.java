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

package com.cobo.coinlib.coins.ETH;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cobo.coinlib.Coinlib;
import com.cobo.coinlib.coins.AbsTx;
import com.cobo.coinlib.coins.SignTxResult;
import com.cobo.coinlib.interfaces.Coin;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.Signer;
import com.cobo.coinlib.utils.Coins;

import org.bouncycastle.util.encoders.Hex;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpType;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cobo.coinlib.Util.concat;
import static com.cobo.coinlib.v8.ScriptLoader.readAsset;
import static org.web3j.crypto.TransactionEncoder.asRlpValues;

public class EthImpl implements Coin {

    private final int chainId;

    public EthImpl(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public String coinCode() {
        return Coins.ETH.coinCode();
    }

    @Override
    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        JSONObject metaData = tx.getMetaData();
        try {
            RawTransaction transaction = createRawTransaction(metaData);
            byte[] signedTransaction = signTransaction(transaction, signers[0]);
            if (signedTransaction == null) {
                callback.onFail();
            } else {
                String txId = "0x" + Hex.toHexString(Hash.sha3(signedTransaction));
                String txHex = "0x" + Hex.toHexString(signedTransaction);
                callback.onSuccess(txId, txHex);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            callback.onFail();
        }
    }


    protected RawTransaction createRawTransaction(JSONObject metaData) throws JSONException {
        String to = metaData.getString("to");
        BigInteger nonce = new BigInteger(String.valueOf(metaData.getInt("nonce")));
        BigInteger gasPrice = new BigInteger(metaData.getString("gasPrice"));
        BigInteger gasLimit = new BigInteger(metaData.getString("gasLimit"));
        BigInteger value = new BigInteger(metaData.getString("value"));
        String contractAddress = metaData.optString("contractAddress");
        String data = "";
        if (!TextUtils.isEmpty(contractAddress)) {
            data = FunctionEncoder.encode(transfer(to, value));
            to = contractAddress;
            value = BigInteger.ZERO;
        }
        return RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, value, data);
    }

    public static JSONObject decodeRawTransaction(String txHex) {
        RawTransaction rawTx = TransactionDecoder.decode(txHex);
        JSONObject metaData = new JSONObject();
        try {
            metaData.put("to", rawTx.getTo());
            metaData.put("nonce", rawTx.getNonce().toString());
            metaData.put("gasPrice", rawTx.getGasPrice().toString());
            metaData.put("gasLimit", rawTx.getGasLimit().toString());
            metaData.put("value", rawTx.getValue().toString());
            String abi = getAbi(Coinlib.sInstance.getContext(), rawTx.getTo());
            String erc20Abi = getAbi(Coinlib.sInstance.getContext(),
                    "0xdac17f958d2ee523a2206206994597c13d831ec7");
            //decode data
            if (abi != null) {
                AbiDecoder decoder = new AbiDecoder();
                decoder.addAbi(erc20Abi);
                decoder.addAbi(abi);
                AbiDecoder.DecodedMethod method = decoder.decodeMethod(rawTx.getData());
                metaData.put("data", method.toJson().toString());
            } else {
                metaData.put("data", rawTx.getData());
            }
            //decode chainId
            if (rawTx instanceof SignedRawTransaction) {
                Sign.SignatureData signatureData = ((SignedRawTransaction) rawTx).getSignatureData();
                byte[] v = signatureData.getV();
                metaData.put("chainId", new BigInteger(v).intValue());
            } else {
                metaData.put("chainId", 1);
            }
            metaData.put("signingData", txHex);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return metaData;
    }

    public static String getSignature(String signedHex) {
        SignedRawTransaction signedTx = (SignedRawTransaction) TransactionDecoder.decode(signedHex);
        Sign.SignatureData signatureData = signedTx.getSignatureData();
        byte[] signatureBytes = concat(concat(signatureData.getR(),signatureData.getS()),signatureData.getV());
        return Hex.toHexString(signatureBytes);
    }

    public SignTxResult signHex(String hex, Signer signer) {
        RawTransaction rawTx = TransactionDecoder.decode(hex);

        byte[] signed = signTransaction(rawTx, signer);
        if (signed != null) {
            String txId = "0x" + Hex.toHexString(Hash.sha3(signed));
            String txHex = "0x" + Hex.toHexString(signed);
            return new SignTxResult(txId, txHex);
        } else {
            return null;
        }
    }

    private static String getAbi(Context context, String contractAddress) {
        AssetManager am = context.getAssets();
        try {
            JSONObject bundleMap = new JSONObject(readAsset(am, "abi/abiMap.json"));
            String abiFile = bundleMap.optString(contractAddress);
            if (!TextUtils.isEmpty(abiFile)) {
                return readAsset(am, "abi/" + abiFile);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] signTransaction(RawTransaction transaction, Signer signer) {
        byte[] encodedTransaction = TransactionEncoder.encode(transaction, chainId);
        byte[] transactionHash = Hash.sha3(encodedTransaction);
        String signature = signer.sign(Hex.toHexString(transactionHash));
        Sign.SignatureData signatureData = getSignatureData(signature);
        return encodeSignedTransaction(transaction, signatureData);
    }

    public Sign.SignatureData getSignatureData(String  signature) {
        if (TextUtils.isEmpty(signature)) return null;
        byte[] r = Hex.decode(signature.substring(0, 64));
        byte[] s = Hex.decode(signature.substring(64, 128));

        int recId = 0;
        try {
            recId = Integer.parseInt(signature.substring(128), 16);
        } catch (Exception ignore) {
        }
        int v = 27 + recId;
        if (chainId > 0) {
            v += chainId * 2 + 8;
        }
        return new Sign.SignatureData((byte) v, r, s);
    }

    private byte[] encodeSignedTransaction(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    public Function transfer(String to, BigInteger value) {
        return new Function(
                "transfer",
                Arrays.asList(new Address(to), new Uint256(value)),
                Collections.singletonList(new TypeReference<Bool>() {
                }));
    }

    @Override
    public String signMessage(@NonNull String message, Signer signer) {
        return null;
    }

    @Override
    public String generateAddress(@NonNull String publicKey) {
        return null;
    }

    @Override
    public boolean isAddressValid(@NonNull String address) {
        return false;
    }
}
