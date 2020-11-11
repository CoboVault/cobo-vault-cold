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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SignerListSet extends XrpTransaction {

    public SignerListSet() { super(Schemas.SignerListSet); }

    @Override
    public JSONObject flatTransactionDetail(JSONObject tx) {
        JSONObject result = new JSONObject();
        try {
            result.put("TransactionType", tx.getString("TransactionType"));
            result.put("Account", tx.getString("Account"));
            result.put("Fee", tx.getString("Fee") + " drops");
            result.put("SignerQuorum", tx.getInt("SignerQuorum"));
            if(tx.has("SignerEntries")){
                JSONArray Signer = tx.optJSONArray("SignerEntries");
                if(null != Signer) {
                    for( int index = 0; index < Signer.length(); index++){
                        JSONObject SignerObj = Signer.optJSONObject(index);
                        if(SignerObj.has("SignerEntry")){
                            JSONObject entry = SignerObj.getJSONObject("SignerEntry");
                            if(entry.has("Account") && entry.has("SignerWeight") ) {
                                result.put("SignerEntry"+ index +".Account", entry.getString("Account"));
                                result.put("SignerEntry"+ index +".SignerWeight", entry.getInt("SignerWeight"));
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  result;
    }
}
