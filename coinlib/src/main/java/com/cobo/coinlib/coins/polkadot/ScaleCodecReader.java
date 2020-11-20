package com.cobo.coinlib.coins.polkadot;

import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class ScaleCodecReader extends com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader {
    public ScaleCodecReader(byte[] source) {
        super(source);
    }

    public BigInteger readCompact() {
        return COMPACT_BIGINT.read(this);
    }

    public String readString(int length) {
        byte[] bytes = readByteArray(length);
        return Hex.toHexString(bytes);
    }

    public byte[] readRestBytes() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        while (hasNext()) {
            bo.write(readByte());
        }
        return bo.toByteArray();
    }

    ;

    public String readRestString() {
        return Hex.toHexString(readRestBytes());
    }
}
