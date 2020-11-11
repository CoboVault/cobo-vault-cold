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

public class EscrowCreate extends XrpTransaction {

    public EscrowCreate() {
        super(Schemas.EscrowCreate);
    }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            result.put("TransactionType", tx.getString("TransactionType"));
            result.put("Account", tx.getString("Account"));
            result.put("Destination", tx.getString("Destination"));
            result.put("Amount", tx.getString("Amount") + " drops");
            if(tx.has("DestinationTag")){
                result.put("DestinationTag", tx.getInt("DestinationTag"));
            }
            if(tx.has("CancelAfter")){
                result.put("CancelAfter", tx.getInt("CancelAfter"));
            }
            if(tx.has("FinishAfter")){
                result.put("FinishAfter", tx.getInt("FinishAfter"));
            }
            if(tx.has("Condition")){
                result.put("Condition", tx.getString("Condition"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
