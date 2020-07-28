/*
 *
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cobo.coinlib;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.Sha256Hash;

import static com.cobo.coinlib.Util.int2bytes;

public enum ExtendPubkeyFormat {
    // https://github.com/satoshilabs/slips/blob/master/slip-0132.md
    // mainnet
    xpub(0x0488b21e, "P2PKH or P2WSH_P2SH"),
    ypub(0x049d7cb2, "P2WPKH in P2WSH_P2SH"),
    zpub(0x04b24746, "P2WPKH"),
    Ypub(0x0295b43f, "Multi-signature P2WSH in P2WSH_P2SH"),
    Zpub(0x02aa7ed3, "Multi-signature P2WSH"),
    //testnet
    tpub(0x043587cf, "P2PKH or P2WSH_P2SH"),
    upub(0x044a5262, "P2WPKH in P2WSH_P2SH"),
    vpub(0x045f1cf6, "P2WPKH"),
    Upub(0x024289ef, "Multi-signature P2WSH in P2WSH_P2SH"),
    Vpub(0x02575483, "Multi-signature P2WSH");

    private final int header;
    private final String addressEncoding;
    ExtendPubkeyFormat(int header, String addressEncoding) {
        this.header = header;
        this.addressEncoding = addressEncoding;
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

    public String getAddressEncoding() {
        return addressEncoding;
    }

    public static boolean isValidXpub(String xpub) {
        try {
            Base58.decodeChecked(xpub);
            return true;
        }catch (Exception ignore) { }
        return false;
    }

    public static boolean isEqualIgnorePrefix(String xpub1, String xpub2) {
        return convertExtendPubkey(xpub1, xpub).equals(convertExtendPubkey(xpub2, xpub));
    }
}
