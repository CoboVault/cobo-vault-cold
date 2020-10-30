package com.cobo.coinlib.coins.DOT.scale.writer;

import com.cobo.coinlib.coins.DOT.scale.ScaleWriter;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecWriter;

import java.io.IOException;

public class UByteWriter implements ScaleWriter<Integer> {

    @Override
    public void write(ScaleCodecWriter wrt, Integer value) throws IOException {
        if (value < 0 || value > 0xff) {
            throw new IllegalArgumentException("Only values in range 0..255 are supported: " + value);
        }
        wrt.directWrite(value);
    }
}
