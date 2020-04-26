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

package com.cobo.coinlib.coins.ETC;

import com.cobo.coinlib.coins.ETH.Eth;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.Coin;
import com.cobo.coinlib.utils.Coins;

import org.json.JSONException;
import org.json.JSONObject;

public class Etc extends Eth {

    public Etc(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.ETC.coinCode();
    }

    public static class Tx extends Eth.Tx {
        public Tx(JSONObject metaData, String coinCode) throws JSONException, InvalidTransactionException {
            super(metaData, coinCode);
        }
    }

    public static class Deriver extends Eth.Deriver {
    }
}
