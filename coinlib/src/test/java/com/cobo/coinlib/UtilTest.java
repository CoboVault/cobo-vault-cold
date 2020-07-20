
/*
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
 */

package com.cobo.coinlib;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilTest {
    @Test
    public void testXpub2Pub() {
        String xpub = "xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb";
        String pub = Util.pubKeyFromExtentPubKey(xpub);
        assertEquals(pub,"0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b8");
    }

    @Test
    public void testXpub2Pub2() {
        String xpub = "xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb";
        String pub = Util.getPublicKeyHex(xpub,"M/49'/0'/0'/0/0");
        assertEquals(pub,"02057656d1036539463e925e9f7f8232120750667b77cde62dcaa31d3011d65c67");

        pub = Util.getPublicKeyHex(xpub,"M/49'/0'/0'/0/10");
        assertEquals(pub,"02a18c6e271a995b162348b4332b63f13bf031617192d6232e809210ad5d85c382");

        pub = Util.getPublicKeyHex(xpub,"M/49'/0'/0'/0/2147483647");
        assertEquals(pub,"032657f51bf6bd3e510e67d5c39235a077d69ac3adac0c260aa471d64bc4dad762");
    }

    @Test
    public void leadingZeros() {
        byte[] bytes = Util.trimOrAddLeadingZeros(new byte[]{0x01});
        assertEquals("0000000000000000000000000000000000000000000000000000000000000001", Hex.toHexString(bytes));

        bytes = Util.trimOrAddLeadingZeros(Hex.decode("000000000000000000000000000000000000000000000000000000000000000001"));
        assertEquals("0000000000000000000000000000000000000000000000000000000000000001", Hex.toHexString(bytes));

        bytes = Util.trimOrAddLeadingZeros(Hex.decode("0000000000000000000000000000000000000000000000000000000001"));
        assertEquals("0000000000000000000000000000000000000000000000000000000000000001", Hex.toHexString(bytes));
    }
    @Test
    public void testXpubToYpub() {
        assertEquals("ypub6XsyMmCyC7o9aXNfXzxwFgz3XPub9HadNzaZraotUtYjRHkJR7YXvaPmdZvvxhrYh9ajWXBJaPNjPsEPo3M4uNG9LyrrPTaYuee44qgWJW3",
                ExtendPubkeyFormat.convertExtendPubkey("xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb", ExtendPubkeyFormat.ypub));
    }

    @Test
    public void testisValidXpub() {
        assertTrue(ExtendPubkeyFormat.isValidXpub("xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb"));
        assertFalse(ExtendPubkeyFormat.isValidXpub("xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGs"));
        assertFalse(ExtendPubkeyFormat.isValidXpub("xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsB"));
    }
}
