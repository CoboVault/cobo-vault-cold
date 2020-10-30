package com.cobo.coinlib.coins.DOT.scale.writer;

import com.cobo.coinlib.coins.DOT.scale.CompactMode;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecWriter;
import com.cobo.coinlib.coins.DOT.scale.ScaleWriter;

import java.io.IOException;
import java.math.BigInteger;

public class CompactBigIntWriter implements ScaleWriter<BigInteger> {

    private static final CompactULongWriter LONG_WRITER = new CompactULongWriter();

    @Override
    public void write(ScaleCodecWriter wrt, BigInteger value) throws IOException {
        CompactMode mode = CompactMode.forNumber(value);

        byte[] data = value.toByteArray();
        int pos = data.length-1;
        int limit = 0;

        if (mode != CompactMode.BIGINT) {
            LONG_WRITER.write(wrt, value.longValue());
            return;
        }

        wrt.directWrite(((data.length - 4) << 2) + mode.getValue());
        while (pos >= 0) {
            wrt.directWrite(data[pos]);
            pos--;
        }
    }
}
