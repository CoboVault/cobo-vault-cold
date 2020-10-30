package com.cobo.coinlib.coins.DOT.scale.writer;

import com.cobo.coinlib.coins.DOT.scale.ScaleWriter;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecWriter;

import java.io.IOException;

public class UInt16Writer implements ScaleWriter<Integer> {
    @Override
    public void write(ScaleCodecWriter wrt, Integer value) throws IOException {
        wrt.directWrite(value & 0xff);
        wrt.directWrite((value >> 8) & 0xff);
    }
}
