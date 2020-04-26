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

package com.cobo.coinlib.coins.XZC;

import com.cobo.coinlib.coins.AbsCoin;
import com.cobo.coinlib.coins.AbsDeriver;
import com.cobo.coinlib.coins.AbsTx;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.Coin;
import com.cobo.coinlib.utils.Coins;

import org.bitcoinj.core.Base58;
import org.bitcoinj.crypto.DeterministicKey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Xzc extends AbsCoin implements Coin {
    public Xzc(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.XZC.coinCode();
    }

    public static class Tx extends AbsTx {

        public Tx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
            super(object, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException {
            parseInput();
            to = metaData.getString("to");
            amount = metaData.getLong("amount") / Math.pow(10, decimal);
            memo = metaData.optString("memo");
            fee = metaData.getLong("fee") / Math.pow(10, decimal);
        }

        private void parseInput() throws JSONException {
            JSONArray inputs = metaData.getJSONArray("inputs");
            for (int i = 0; i < inputs.length(); i++) {
                JSONObject input = inputs.getJSONObject(i);
                int index = input.optInt("outputIndex");
                if (index == 0) {
                    input.put("outputIndex", 0);
                }
            }
        }
    }

    public static class Deriver extends AbsDeriver {

        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {
            DeterministicKey address = getAddrDeterministicKey(xPubKey, changeIndex, addrIndex);
            return Base58.encodeChecked(0x52, address.getPubKeyHash());
        }

        @Override
        public String derive(String xPubKey) {
            return Base58.encodeChecked(0x52, getDeterministicKey(xPubKey).getPubKeyHash());
        }
    }
}
