package com.cobo.coinlib.coins.DOT.scale.reader;

import com.cobo.coinlib.coins.DOT.scale.ScaleReader;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecReader;

public class BoolReader implements ScaleReader<Boolean> {
    @Override
    public Boolean read(ScaleCodecReader rdr) {
        byte b = rdr.readByte();
        if (b == 0) {
            return false;
        }
        if (b == 1) {
            return true;
        }
        throw new IllegalStateException("Not a boolean value: " + b);
    }
}
