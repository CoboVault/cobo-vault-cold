package com.cobo.coinlib.coins.polkadot.scale.reader;

import com.cobo.coinlib.coins.polkadot.scale.ScaleReader;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class UInt16Reader implements ScaleReader<Integer> {

    @Override
    public Integer read(ScaleCodecReader rdr) {
        int result = 0;
        result += rdr.readUByte();
        result += rdr.readUByte() << 8;
        return result;
    }

    public Integer readBE(ScaleCodecReader rdr) {
        int result = 0;
        result += rdr.readUByte() << 8;
        result += rdr.readUByte();
        return result;
    }

}