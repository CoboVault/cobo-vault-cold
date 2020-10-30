package com.cobo.coinlib.coins.DOT.scale;

/**
 * SCALE codec reader for a complex data type
 * @param <T> type
 */
public interface ScaleReader<T> {

    /**
     * Reads value from specified reader. The reader must be positioned on the beginning of the value
     *
     * @param rdr reader with the encoded data
     * @return read value
     */
    T read(ScaleCodecReader rdr);

}
