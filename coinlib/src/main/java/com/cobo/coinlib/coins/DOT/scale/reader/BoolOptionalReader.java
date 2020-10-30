package com.cobo.coinlib.coins.DOT.scale.reader;

import com.cobo.coinlib.coins.DOT.scale.ScaleReader;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecReader;

import java.util.Optional;

public class BoolOptionalReader implements ScaleReader<Optional<Boolean>> {
    @Override
    public Optional<Boolean> read(ScaleCodecReader rdr) {
        byte b = rdr.readByte();
        if (b == 0) {
            return Optional.empty();
        }
        if (b == 1) {
            return Optional.of(false);
        }
        if (b == 2) {
            return Optional.of(true);
        }
        throw new IllegalStateException("Not a boolean option: " + b);
    }
}
