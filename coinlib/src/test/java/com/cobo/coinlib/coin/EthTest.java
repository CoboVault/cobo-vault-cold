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

import com.cobo.coinlib.coins.ETH.Eth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class EthTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "0xE410157345be56688F43FF0D9e4B2B38Ea8F7828",
                "0xEEACb7a5e53600c144C0b9839A834bb4b39E540c",
                "0xA116800A72e56f91cF1677D40C9984f9C9f4B2c7",
                "0x4826BadaBC9894B3513e23Be408605611b236C0f",
                "0x8a1503beb17Ef02cC4Ff288b0A73583c4ce547c7",
                "0x996c7a5c9001da0401B61aD68EFd2424633F728E",
                "0xcAC3561d0b4Bec860ADCEBD70f8E6a8A5D45D752",
                "0x7173684414e3a9d5347e5d73b1E8718f3020A296",
                "0x4AAb5aD4FF70D7388182068a0B74BEEBA28B5068",
                "0x824C70B0000Abf51F6db46284dC217579f53f86a",
                "0x56FA9453B22867E0292301b68C2A737D8879441B",
                };
        String pubKey = "xpub6CNhtuXAHDs84AhZj5ALZB6ii4sP5LnDXaKDSjiy6kcBbiysq89cDrLG29poKvZtX9z4FchZKTjTyiPuDeiFMUd1H4g5zViQxt4tpkronJr";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Eth.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

}










