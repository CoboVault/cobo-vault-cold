/*
 *
 *  Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cobo.coinlib.coins.XRP.xumm.transcationtype;

import com.cobo.coinlib.coins.XRP.xumm.Schemas;
import com.cobo.coinlib.coins.XRP.xumm.XrpTransaction;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentChannelFund extends XrpTransaction {

    public PaymentChannelFund() { super(Schemas.PaymentChannelFund); }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            result.put("TransactionType", tx.getString("TransactionType"));
            result.put("Account", tx.getString("Account"));
            result.put("Amount", tx.getString("Amount") + " drops");
            result.put("Channel", tx.getString("Channel"));
            if(tx.has("Expiration")) {
                result.put("Expiration", tx.getInt("Expiration"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
