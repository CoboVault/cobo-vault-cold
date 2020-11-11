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

public class OfferCreate extends XrpTransaction {

    public OfferCreate() {
        super(Schemas.OfferCreate);
    }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            result.put("TransactionType", tx.getString("TransactionType"));
            result.put("Account", tx.getString("Account"));
            result.put("Fee", tx.getString("Fee") + " drops");
            if(tx.has("TakerGets")){
                if(null != tx.optJSONObject("TakerGets")) {
                    JSONObject amount = tx.getJSONObject("TakerGets");
                    if(amount.has("value") && amount.has("currency") && amount.has("issuer")) {
                        result.put("TakerGets.value", amount.getString("value"));
                        result.put("TakerGets.currency", amount.getString("currency"));
                        result.put("TakerGets.issuer", amount.getString("issuer"));
                    }
                } else {
                    result.put("TakerGets", tx.getString("TakerGets") + " drops");
                }
            }
            if(tx.has("TakerPays")){
                if(null != tx.optJSONObject("TakerPays")) {
                    JSONObject amount = tx.getJSONObject("TakerPays");
                    if(amount.has("value") && amount.has("currency") && amount.has("issuer")) {
                        result.put("TakerPays.value", amount.getString("value"));
                        result.put("TakerPays.currency", amount.getString("currency"));
                        result.put("TakerPays.issuer", amount.getString("issuer"));
                    }
                } else {
                    result.put("TakerPays", tx.getString("TakerPays") + " drops");
                }
            }
            if(tx.has("Expiration")){
                result.put("Expiration", tx.getInt("Expiration"));
            }
            if(tx.has("OfferSequence")){
                result.put("OfferSequence", tx.getInt("OfferSequence"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
