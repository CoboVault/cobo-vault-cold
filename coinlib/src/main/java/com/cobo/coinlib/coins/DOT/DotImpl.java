package com.cobo.coinlib.coins.DOT;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cobo.coinlib.coins.AbsTx;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecWriter;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.Signer;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class DotImpl {
    private String genesisHash;

    public DotImpl(String coinCode) {
        if (coinCode.equals("DOT")){
            this.genesisHash = "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3";
        }
        else {
            this.genesisHash = "b0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe";
        }
    }

    public static class SubstrateTransactionInfo {
        public long amount;
        public String dest;
        public String blockHash;
        public long nonce;
        public long tip;
        public long transactionVersion;
        public long specVersion;
        public long validityPeriod;
        public int blockNumber;

        public SubstrateTransactionInfo() {
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public void setDest(String dest) {
            this.dest = dest;
        }

        public void setBlockHash(String blockHash) {
            this.blockHash = blockHash;
        }

        public void setNonce(long nonce) {
            this.nonce = nonce;
        }

        public void setTip(long tip) {
            this.tip = tip;
        }

        public void setTransactionVersion(long transactionVersion) {
            this.transactionVersion = transactionVersion;
        }

        public void setSpecVersion(long specVersion) {
            this.specVersion = specVersion;
        }

        public void setValidityPeriod(long validityPeriod) {
            this.validityPeriod = validityPeriod;
        }

        public void setBlockNumber(int blockNumber) {
            this.blockNumber = blockNumber;
        }
    }

    public byte[] constructTransaction(SubstrateTransactionInfo tx) throws Exception {

        ScaleCodecWriter codecWriter = new ScaleCodecWriter(new ByteArrayOutputStream());
        //transferKeepAlive
        codecWriter.writeByte(0x05);
        codecWriter.writeByte(0x03);

        byte[] publicKey = AddressCodec.decodeAddress(tx.dest);

        codecWriter.writeByteArray(publicKey);
        codecWriter.writeBIntCompact(BigInteger.valueOf(tx.amount));

        byte[] mortalEra = constructEra(tx.blockNumber, tx.validityPeriod);
        codecWriter.writeByteArray(mortalEra);

        codecWriter.writeLIntCompact(tx.nonce);

        codecWriter.writeLIntCompact(tx.tip);

        codecWriter.writeUint32(tx.specVersion);

        codecWriter.writeUint32(tx.transactionVersion);

        codecWriter.writeByteArray(Hex.decode(genesisHash));
        codecWriter.writeByteArray(Hex.decode(tx.blockHash));

        return codecWriter.toByteArray();
    }

    public byte[] constructSignedTransaction(String publicKey, String signature, SubstrateTransactionInfo tx) throws Exception {
        ScaleCodecWriter scaleCodecWriter = new ScaleCodecWriter(new ByteArrayOutputStream());
        scaleCodecWriter.writeByte(0x84);
        scaleCodecWriter.writeByteArray(Hex.decode(publicKey));
        scaleCodecWriter.writeByte(0x01);
        scaleCodecWriter.writeByteArray(Hex.decode(signature));
        byte[] mortalEra = constructEra(tx.blockNumber, tx.validityPeriod);
        scaleCodecWriter.writeByteArray(mortalEra);
        scaleCodecWriter.writeLIntCompact(tx.nonce);
        scaleCodecWriter.writeLIntCompact(tx.tip);
        scaleCodecWriter.writeByte(0x05);
        scaleCodecWriter.writeByte(0x03);
        byte[] destPublicKey = AddressCodec.decodeAddress(tx.dest);
        scaleCodecWriter.writeByteArray(destPublicKey);
        scaleCodecWriter.writeBIntCompact(BigInteger.valueOf(tx.amount));
        byte[] txContent = scaleCodecWriter.toByteArray();

        ScaleCodecWriter finalWriter = new ScaleCodecWriter(new ByteArrayOutputStream());
        finalWriter.writeCompact(txContent.length);
        finalWriter.writeByteArray(txContent);

        return finalWriter.toByteArray();
    }

    public void generateTransaction(@NonNull AbsTx tx, SignCallback callback, Signer... signers) {
        JSONObject metadata = tx.getMetaData();
        try {
            SubstrateTransactionInfo substrateTransactionInfo = new SubstrateTransactionInfo();

            long amount = metadata.getLong("value");
            String dest = metadata.getString("dest");
            String blockHash = metadata.getString("blockHash");
            long nonce = metadata.getLong("nonce");
            long tip = metadata.optLong("tip");
            long transactionVersion = metadata.getLong("transactionVersion");
            long specVersion = metadata.getLong("specVersion");
            long validityPeriod = metadata.optLong("validityPeriod");
            int blockNumber = metadata.getInt("blockNumber");

            substrateTransactionInfo.setAmount(amount);
            substrateTransactionInfo.setDest(dest);
            substrateTransactionInfo.setBlockHash(blockHash);
            substrateTransactionInfo.setNonce(nonce);
            substrateTransactionInfo.setTip(tip);
            substrateTransactionInfo.setTransactionVersion(transactionVersion);
            substrateTransactionInfo.setSpecVersion(specVersion);
            substrateTransactionInfo.setValidityPeriod(validityPeriod > 0 ? validityPeriod : 4096);
            substrateTransactionInfo.setBlockNumber(blockNumber);

            byte[] transaction = constructTransaction(substrateTransactionInfo);
            String result = signers[0].sign(Hex.toHexString(transaction));
            String publicKey = signers[0].getPublicKey();
            if (TextUtils.isEmpty(result)) {
                callback.onFail();
            } else {
                byte[] signedTx = constructSignedTransaction(publicKey, result, substrateTransactionInfo);
                String txId = Hex.toHexString(AddressCodec.blake2b(signedTx, 256));
                callback.onSuccess(txId, result);
            }
        } catch (Exception e) {

        }

    }

    private byte[] constructEra(int blockNumber, long eraPeriod) {
        int calPeriod = (int) Math.pow(2, Math.ceil(Math.log(eraPeriod) / Math.log(2)));

        calPeriod = Math.min(Math.max(calPeriod, 4), 1 << 16);

        int phase = blockNumber % calPeriod;

        int quantizeFactor = Math.max(calPeriod >> 12, 1);
        int quantizedPhase = phase / quantizeFactor * quantizeFactor;

        int trailingZeros = getTrailingZeros(eraPeriod);

        int encoded = Math.min(15, Math.max(1, trailingZeros - 1)) + (((quantizedPhase / quantizeFactor) << 4));
        byte first = (byte) (encoded >> 8);
        byte second = (byte) ((byte) encoded & 0xff);

        return new byte[]{second, first};
    }

    private int getTrailingZeros(Long period) {
        String binary = Long.toString(period, 2);
        int index = 0;

        while (binary.toCharArray()[binary.length() - 1 - index] == '0') {
            index++;
        }

        return index;
    }
}
