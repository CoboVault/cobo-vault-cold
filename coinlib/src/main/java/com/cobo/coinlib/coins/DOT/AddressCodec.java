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

package com.cobo.coinlib.coins.DOT;

import org.bitcoinj.core.Base58;
import org.bouncycastle.crypto.digests.Blake2bDigest;

import java.util.Arrays;

import static com.cobo.coinlib.Util.concat;

public class AddressCodec {
    private final static byte[] SS58_PREFIX = "SS58PRE".getBytes();

    public static String encodeAddress(byte[] key) {
        return encodeAddress(key, (byte) 42);
    }

    public static String encodeAddress(byte[] key, byte prefix) {
        byte[] input = concat(new byte[]{prefix}, key);
        byte[] hash = sshash(input);
        byte[] bytes = concat(input, Arrays.copyOfRange(hash, 0, 2));
        return Base58.encode(bytes);
    }

    public static byte[] sshash(byte[] key) {
        return blake2b(concat(SS58_PREFIX, key), 512);
    }

    public static byte[] blake2b(byte[] data, int bitLength) {
        int byteLength = (int) Math.ceil(bitLength / 8F);
        Blake2bDigest digest = new Blake2bDigest(null, byteLength, null, null);
        digest.reset();
        digest.update(data, 0, data.length);
        byte[] keyedHash = new byte[64];
        int digestLength = digest.doFinal(keyedHash, 0);
        return Arrays.copyOfRange(keyedHash, 0, digestLength);
    }
}