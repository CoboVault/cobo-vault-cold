package com.cobo.coinlib.coins.DOT;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cobo.coinlib.coins.AbsTx;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.Signer;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

public class DotImpl {
    private final ChainProperty chainProperty;

    public DotImpl(String coinCode) {
        chainProperty = ChainProperty.of(coinCode);
    }

    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        JSONObject metadata = tx.getMetaData();
        try {
            long amount = metadata.getLong("value");
            String dest = metadata.getString("dest");
            String blockHash = metadata.getString("blockHash");
            long nonce = metadata.getLong("nonce");
            long tip = metadata.optLong("tip");
            long transactionVersion = metadata.getLong("transactionVersion");
            long specVersion = metadata.getLong("specVersion");
            long validityPeriod = metadata.optLong("validityPeriod");
            int blockNumber = metadata.getInt("blockNumber");
            TransactionEncoderBuilder builder = new TransactionEncoderBuilder();

            TransactionEncoder txEncoder = builder.setChainProperty(chainProperty)
                    .setAmount(amount)
                    .setDest(dest)
                    .setBlockHash(blockHash)
                    .setNonce(nonce)
                    .setTip(tip)
                    .setTransactionVersion(transactionVersion)
                    .setSpecVersion(specVersion)
                    .setValidityPeriod(validityPeriod > 0 ? validityPeriod : 4096)
                    .setBlockNumber(blockNumber)
                    .setFrom(AddressCodec.encodeAddress(Hex.decode(signers[0].getPublicKey()), chainProperty.addressPrefix))
                    .createSubstrateTransactionInfo();

            byte[] transaction = txEncoder.encode();
            String result = signers[0].sign(Hex.toHexString(transaction));
            if (TextUtils.isEmpty(result)) {
                callback.onFail();
            } else {
                txEncoder.addSignature(result);
                byte[] signedTx = txEncoder.encode();
                String txId = Hex.toHexString(AddressCodec.blake2b(signedTx, 256));
                callback.onSuccess(txId, result);
            }
        } catch (Exception ignored) {

        }

    }
}
