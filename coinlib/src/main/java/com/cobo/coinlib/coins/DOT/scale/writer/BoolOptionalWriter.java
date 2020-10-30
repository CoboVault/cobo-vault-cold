package com.cobo.coinlib.coins.DOT.scale.writer;

import com.cobo.coinlib.coins.DOT.scale.ScaleWriter;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecWriter;

import java.io.IOException;
import java.util.Optional;

public class BoolOptionalWriter implements ScaleWriter<Optional<Boolean>> {

    @Override
    public void write(ScaleCodecWriter wrt, Optional<Boolean> value) throws IOException {
        if (!value.isPresent()) {
            wrt.directWrite(0);
        } else if (value.get()) {
            wrt.directWrite(2);
        } else {
            wrt.directWrite(1);
        }
    }
}
