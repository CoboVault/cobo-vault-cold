package com.cobo.coinlib.coins.DOT.scale.reader;

import com.cobo.coinlib.coins.DOT.scale.ScaleCodecReader;
import com.cobo.coinlib.coins.DOT.scale.ScaleReader;

/**
 * Read string, encoded as UTF-8 bytes
 */
public class StringReader implements ScaleReader<String> {
    @Override
    public String read(ScaleCodecReader rdr) {
        return rdr.readString();
    }
}
