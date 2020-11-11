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

package com.cobo.coinlib.coins.XRP.transcationtype;

import com.cobo.coinlib.coins.XRP.Schemas;
import com.cobo.coinlib.coins.XRP.XrpTransaction;

import org.json.JSONException;
import org.json.JSONObject;

public class TrustSet extends XrpTransaction {

    public TrustSet() { super(Schemas.TrustSet); }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            result.put("TransactionType", tx.getString("TransactionType"));
            result.put("Account", tx.getString("Account"));
            result.put("Fee", tx.getString("Fee") + " drops");
            if(tx.has("LimitAmount")){
                if(null != tx.optJSONObject("LimitAmount")) {
                    JSONObject amount = tx.getJSONObject("LimitAmount");
                    if(amount.has("value") && amount.has("currency") && amount.has("issuer")) {
                        result.put("LimitAmount.value", amount.getString("value"));
                        result.put("LimitAmount.currency", amount.getString("currency"));
                        result.put("LimitAmount.issuer", amount.getString("issuer"));
                    }
                } else {
                    result.put("LimitAmount", tx.getString("LimitAmount") + " drops");
                }
            }
            if(tx.has("QualityIn")){
                result.put("QualityIn", tx.getInt("QualityIn"));
            }
            if(tx.has("QualityOut")){
                result.put("QualityOut", tx.getInt("QualityOut"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
