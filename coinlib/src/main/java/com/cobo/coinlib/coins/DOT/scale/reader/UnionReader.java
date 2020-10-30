package com.cobo.coinlib.coins.DOT.scale.reader;

import com.cobo.coinlib.coins.DOT.scale.ScaleReader;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecReader;
import com.cobo.coinlib.coins.DOT.scale.UnionValue;

import java.util.*;

public class UnionReader<T> implements ScaleReader<UnionValue<T>> {

    private final List<ScaleReader<? extends T>> mapping;

    public UnionReader(List<ScaleReader<? extends T>> mapping) {
        this.mapping = mapping;
    }

    @SuppressWarnings("unchecked")
    public UnionReader(ScaleReader<? extends T>... mapping) {
        this(Arrays.asList(mapping));
    }

    @Override
    @SuppressWarnings("unchecked")
    public UnionValue<T> read(ScaleCodecReader rdr) {
        int index = rdr.readUByte();
        if (mapping.size() <= index) {
            throw new IllegalStateException("Unknown type index: " + index);
        }
        T value = (T) mapping.get(index).read(rdr);
        return new UnionValue<>(index, value);
    }
}
