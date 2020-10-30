package com.cobo.coinlib.coins.DOT;

import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class ScaleCodecReader extends io.emeraldpay.polkaj.scale.ScaleCodecReader {
    public ScaleCodecReader(byte[] source) {
        super(source);
    }

    public BigInteger readCompact() {
        try{
            return new BigInteger(String.valueOf(COMPACT_UINT.read(this)));
        }
        catch (Exception e){
            return COMPACT_BIGINT.read(this);
        }
    }

    public String readString(int length){
        byte[] bytes = readByteArray(length);
        return Hex.toHexString(bytes);
    }

    public byte[] readRestBytes() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        while (hasNext()) {
            bo.write(readByte());
        }
        return bo.toByteArray();
    };

    public String readRestString() {
        return Hex.toHexString(readRestBytes());
    }
}
