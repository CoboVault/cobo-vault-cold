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

package com.cobo.coinlib.coins.XRP;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class XrpTransaction {

    protected String schema;
    private final int decimals = 6;
    private static final long RippleEpochSeconds = 946684800L;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
            Locale.getDefault());

    protected XrpTransaction(String schema) {
        this.schema = schema;
    }

    public String getTransactionType() {
        return getClass().getSimpleName();
    }

    public abstract JSONObject flatTransactionDetail(JSONObject tx);

    public boolean isValid(JSONObject tx) {
        try {
            return getTransactionType().equals(tx.getString("TransactionType"))
                    && new JsonSchemaValidator().isStateValid(schema, tx.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected String formatAmount(String drops) {
        try {
            return new BigDecimal(drops)
                    .divide(BigDecimal.TEN.pow(decimals), decimals, BigDecimal.ROUND_HALF_UP)
                    .stripTrailingZeros().toPlainString() + " XRP";
        } catch (Exception e) {
            return "0 XRP";
        }
    }

    public String formatTimeStamp(int time) {
        return formatter.format((RippleEpochSeconds + time) * 1e3);
    }
}
