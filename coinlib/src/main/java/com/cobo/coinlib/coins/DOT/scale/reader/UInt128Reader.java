package com.cobo.coinlib.coins.DOT.scale.reader;

import com.cobo.coinlib.coins.DOT.scale.ScaleCodecReader;
import com.cobo.coinlib.coins.DOT.scale.ScaleReader;

import java.math.BigInteger;

public class UInt128Reader implements ScaleReader<BigInteger> {

    public static final int SIZE_BYTES = 16;

    public static void reverse(byte[] value) {
        for (int i = 0; i < value.length / 2; i++) {
            int other = value.length - i - 1;
            byte tmp = value[other];
            value[other] = value[i];
            value[i] = tmp;
        }
    }

    @Override
    public BigInteger read(ScaleCodecReader rdr) {
        byte[] value = rdr.readByteArray(SIZE_BYTES);
        reverse(value);
        return new BigInteger(1, value);
    }
}
