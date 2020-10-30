package com.cobo.coinlib.coins.DOT.scale.writer;

import com.cobo.coinlib.coins.DOT.scale.ScaleWriter;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecWriter;

import java.io.IOException;

public class BoolWriter implements ScaleWriter<Boolean> {
    @Override
    public void write(ScaleCodecWriter wrt, Boolean value) throws IOException {
        if (value) {
            wrt.directWrite(1);
        } else {
            wrt.directWrite(0);
        }
    }
}
