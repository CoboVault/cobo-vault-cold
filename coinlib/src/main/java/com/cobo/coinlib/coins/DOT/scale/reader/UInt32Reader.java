package com.cobo.coinlib.coins.DOT.scale.reader;

import com.cobo.coinlib.coins.DOT.scale.ScaleReader;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecReader;

public class UInt32Reader implements ScaleReader<Long> {
    @Override
    public Long read(ScaleCodecReader rdr) {
        long result = 0;
        result += (long)rdr.readUByte();
        result += ((long)rdr.readUByte()) << 8;
        result += ((long)rdr.readUByte()) << (2 * 8);
        result += ((long)rdr.readUByte()) << (3 * 8);
        return result;
    }
}
