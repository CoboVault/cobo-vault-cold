package com.cobo.coinlib.coins.DOT.scale.reader;

import com.cobo.coinlib.coins.DOT.scale.ScaleReader;
import com.cobo.coinlib.coins.DOT.scale.ScaleCodecReader;

import java.util.ArrayList;
import java.util.List;

public class ListReader<T> implements ScaleReader<List<T>> {

    private ScaleReader<T> scaleReader;

    public ListReader(ScaleReader<T> scaleReader) {
        this.scaleReader = scaleReader;
    }

    @Override
    public List<T> read(ScaleCodecReader rdr) {
        int size = rdr.readCompactInt();
        List<T> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(rdr.read(scaleReader));
        }
        return result;
    }
}
