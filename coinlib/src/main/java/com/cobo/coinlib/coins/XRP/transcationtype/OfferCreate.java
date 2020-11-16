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
            flatTransactionCommonFields(result, tx);
            if(tx.has("TakerGets")){
                if(null != tx.optJSONObject("TakerGets")) {
                    JSONObject amount = tx.optJSONObject("TakerGets");
                    if(amount.has("value") && amount.has("currency") && amount.has("issuer")) {
                        result.putOpt("TakerGets.value", amount.opt("value"));
                        result.putOpt("TakerGets.currency", amount.opt("currency"));
                        result.putOpt("TakerGets.issuer", amount.opt("issuer"));
                    }
                } else {
                    result.putOpt("TakerGets", formatAmount(tx.optString("TakerGets")));
                }
            }
            if(tx.has("TakerPays")){
                if(null != tx.optJSONObject("TakerPays")) {
                    JSONObject amount = tx.optJSONObject("TakerPays");
                    if(amount.has("value") && amount.has("currency") && amount.has("issuer")) {
                        result.putOpt("TakerPays.value", amount.opt("value"));
                        result.putOpt("TakerPays.currency", amount.opt("currency"));
                        result.putOpt("TakerPays.issuer", amount.opt("issuer"));
                    }
                } else {
                    result.putOpt("TakerPays", formatAmount(tx.optString("TakerPays")));
                }
            }
            result.putOpt("Expiration", formatTimeStamp(tx.optInt("Expiration")));
            result.putOpt("OfferSequence", tx.opt("OfferSequence"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
