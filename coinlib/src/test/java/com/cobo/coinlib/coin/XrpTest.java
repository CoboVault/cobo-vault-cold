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

import com.cobo.coinlib.coins.XRP.Xrp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ALL")
public class XrpTest {
    @Test
    public void deriveAddress() {
        String[] addr = new String[] {
                "rndm7RphBZG6CpZvKcG9AjoFbSvcKhwLCx",
                "rrBD4sBsxrpzbohAEYWH4moPSsoxupWLA",
                "rsc38kSbRZ74VjiNa8CG8xtkdqw2AWaXBb",
                "r4Mh3Hdvk1UJJSs8tjkz9qnbxNyMD5qhYz",
                "rsR6GtwgEtcJRaMrW2cNx8nwNqFovnJ32C",
                "rhzrij6yt1wCwRAFgQK5VqxyxyhbNw7QR9",
                "rNLCXobmiL4LbQkbjFJCWSW6XQm8XDLoCq",
                "rKaNnXijwXQhyWegPkmUYchJzAxgKQjry9",
                "rJ1gcRd2w38wwFNdSiqqVEuf4jYHU1fpP1",
                "rE8fnyfbtdwkbumCm3aRR5dWcTHvS6pnWt",
                "rLJYeuBpLdo6CY3xhc3SWt7hzYS6votewa",
                };
        String pubKey = "xpub6C438jHkPCDoEy5jAH4a9hBtYrcprSwGvEA8L5HNhqDyJa1WZPpZXj9DNNtsRjcHxzsuZJq18sMSkbmqYKqpDacP8aMSK63ExzX2bPoMdAo";
        for (int i = 0 ; i < addr.length; i++) {
            String address = new Xrp.Deriver().derive(pubKey,0,i);
            assertEquals(address,addr[i]);
        }
    }

}










