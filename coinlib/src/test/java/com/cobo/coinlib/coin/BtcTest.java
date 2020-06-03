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

package com.cobo.coinlib.coin;

import com.cobo.coinlib.coins.BTC.Btc;
import com.cobo.coinlib.coins.BTC.Deriver;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class BtcTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "3Kd5rjiLtvpHv5nhYQNTTeRLgrz4om32PJ",
                "352BwoAuZWavjSDgAUUCnbg6a2v3FEzbMn",
                "32Hhx9CM3gYwChY2ZRFtHTYpJCeUaoypkj",
                "3C5VmDeH3x6x9fPm4cft3qVhzvv8R4Ln7K",
                "38c8FFS9W4QEW55WXgo2wX8HZJCutH89VT",
                "3EWeeC2wPyTAsPG7rTRbczbFjvZ8nW41AZ",
                "35yk3RdCHQYptsZAmibMpUt6F9XkK7UVCm",
                "32sNjcbmj4hqthuP6hsQk3BjzVLFLZSLr8",
                "34dKWKb347TJwp1PysTt5faqSs9C1PXrVX",
                "3AzuY1VfwKBgAN3soFnm8BNNAJxx4S3Y7L",
                "3CAgK3r8nDiMcdnqBUL3Yy7JMFDjvkXqHn"
                };
        String pubKey = "xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Deriver().derive(pubKey,0,i, Btc.AddressType.P2SH);
            assertEquals(address,addr[i]);
        }

        addr = new String[]{
                "1NKH6iXtjQjzjndJsKJogj5AbwTJZYeqDX",
                "15NxdDkZkiVtze5GVmYwTw2R1BpKekyz1g",
                "1PGcHJ7Uz2dm8qDjbCR4UVvMyyEpbUX1Bf",
                "1GwwKRB7f5eUfkQXpznJHfvcfE77rNo9dY",
                "1A3S3Vp9gMBiXAUb287FCuCvGpCUvBxf1",
                "15z9mWewNTBMGeX1oQAQRj4ko9j56wzSnq",
                "1KmyhQKERuDwaXgGD4wxvSAavxGhxjFdCa",
                "1KtoDzC8RJQEUGFGo6awDXfLKX2DzdwDmd",
                "1BrrVPCciPnvSpYMWjAunMhFYfsN1a2SsZ",
                "15MprQnv9rBgWwdMP5Gav5atYSvMFBmSf4"
        };
        pubKey = "xpub6CHASsYUXq4Z1vSdcayG7v7mjUoABEcRBEKWjoEGBKmjKsEZ765J9ivEpYqcznQbnCmMFnJSkKp5f58sAgDUyfCfab5qq9eQjBBjpsb6tju";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Deriver().derive(pubKey,0,i, Btc.AddressType.P2PKH);
            assertEquals(address,addr[i]);
        }
        addr = new String[] {
                "bc1q9unqc738dxjg5mk8zqtz33zg59cahrj29s24lp",
                "bc1qxgjq769zjm4npchn3d5p0pl0w32qexycxt3efv",
                "bc1qd8jkgnrt4qgnneutqrz6s8tzncdjtztsvhhnmc",
                "bc1qe2wrgssc8fkvjhwy3mcypcqcqnpf02zkmtthvj",
                "bc1q8tkjkn0fdf775xy670dvwcxg3pvdkg93qqpule",
                "bc1qech02hjkr0s4aajae3ulnxq30c2z6cqkfj0umf",
                "bc1qtksmexnnpdlf6gylzkhlnsyk76aa38fxqggk6u",
                "bc1qmr785dwsgjk7genr3u2dy3rxvjwug9s5zdk5q5",
                "bc1q568s0pc4859remhuqtgpf90ssrug29t4uzp796",
                "bc1quexfwqe4kxgchzhupecws7ld4xqfdu8es9jpat"
        };
        pubKey = "zpub6rcabYFcdr41zyUNRWRyHYs2Sm86E5XV8RjjRzTFYsiCngteeZnkwaF2xuhjmM6kpHjuNpFW42BMhzPmFwXt48e1FhddMB7xidZzN4SF24K";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Deriver().derive(pubKey,0,i, Btc.AddressType.SegWit);
            assertEquals(address,addr[i]);
        }
    }

}
