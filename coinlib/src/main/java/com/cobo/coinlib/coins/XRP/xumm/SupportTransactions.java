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

package com.cobo.coinlib.coins.XRP.xumm;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SupportTransactions {
    private static final Map<String,XrpTransaction> sMap;

    static {
        sMap = new HashMap<>();
        register(new TrustSet());
    }

    @Nullable
    public static XrpTransaction get(String type) {
        return sMap.get(type);
    }

    private static void register(XrpTransaction xrpTransaction) {
        sMap.put(xrpTransaction.getTransactionType(), xrpTransaction);
    }
}
