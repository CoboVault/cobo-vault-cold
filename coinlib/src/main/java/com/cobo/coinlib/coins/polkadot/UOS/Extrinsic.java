package com.cobo.coinlib.coins.polkadot.UOS;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.pallets.PalletFactory;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Extrinsic {
    private final byte[] rawSigningPayload;
    private final Network network;

    public Parameter palletParameter;
    private String era;
    private BigInteger nonce;
    private BigInteger tip;
    private long specVersion;
    private long transactionVersion;
    private String genesisHash;
    private String blockHash;

    public Extrinsic(byte[] rawSigningPayload, Network network) {
        this.rawSigningPayload = rawSigningPayload;
        this.network = network;
        read();
    }

    private void read() {
        ScaleCodecReader scr = new ScaleCodecReader(rawSigningPayload);
        int code = scr.readUint16BE();
        Pallet pallet = PalletFactory.getPallet(code, network);
        if (pallet != null) {
            palletParameter = pallet.read(scr);
            era = scr.readString(2);
            nonce = scr.readCompact();
            tip = scr.readCompact();
            specVersion = scr.readUint32();
            transactionVersion = scr.readUint32();
            genesisHash = scr.readString(32);
            blockHash = scr.readString(32);
        }
    }

    public String getEra() {
        return era;
    }

    public String getNonce() {
        return nonce.toString();
    }

    public String getTip() {
        if (tip.equals(BigInteger.ZERO)) return "0";
        return new BigDecimal(tip)
                .divide(BigDecimal.TEN.pow(network.decimals), Math.min(network.decimals, 8), BigDecimal.ROUND_HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    public long getSpecVersion() {
        return specVersion;
    }

    public long getTransactionVersion() {
        return transactionVersion;
    }

    public String getGenesisHash() {
        return genesisHash;
    }

    public String getBlockHash() {
        return blockHash;
    }
}
