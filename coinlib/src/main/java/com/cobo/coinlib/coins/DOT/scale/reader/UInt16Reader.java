package com.cobo.coinlib.coins.DOT.scale.reader;

import com.cobo.coinlib.coins.DOT.scale.ScaleReader;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecReader;

public class UInt16Reader implements ScaleReader<Integer> {

    @Override
    public Integer read(ScaleCodecReader rdr) {
        int result = 0;
        result += rdr.readUByte();
        result += rdr.readUByte() << 8;
        return result;
    }

}
