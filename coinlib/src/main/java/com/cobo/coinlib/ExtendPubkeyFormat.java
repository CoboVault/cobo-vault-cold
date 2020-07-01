package com.cobo.coinlib;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;

import static com.cobo.coinlib.coins.BTC.Electrum.TxUtils.int2bytes;

public enum ExtendPubkeyFormat {
    XPUB(0x0488b21e),
    YPUB(0x049d7cb2),
    ZPUB(0x04b24746);

    private int header;
    ExtendPubkeyFormat(int header) {
        this.header = header;
    }

    public static String convertExtendPubkey(String xpub, ExtendPubkeyFormat targetFormat) {
        int header = targetFormat.getHeader();
        byte[] bytes = Base58.decodeChecked(xpub);
        byte[] result = new byte[bytes.length + 4];
        System.arraycopy(int2bytes(header), 0, bytes, 0, 4);
        byte[] checksum = Sha256Hash.hashTwice(bytes, 0, bytes.length);
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        System.arraycopy(checksum, 0, result, bytes.length, 4);
        return Base58.encode(result);
    }

    public int getHeader() {
        return header;
    }
}
