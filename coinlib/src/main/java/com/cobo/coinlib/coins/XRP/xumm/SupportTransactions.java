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

import androidx.annotation.Nullable;

import com.cobo.coinlib.coins.XRP.xumm.transcationtype.AccountDelete;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.AccountSet;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.CheckCancel;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.CheckCash;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.CheckCreate;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.DepositPreauth;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.EscrowCancel;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.EscrowCreate;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.EscrowFinish;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.OfferCancel;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.OfferCreate;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.Payment;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.PaymentChannelClaim;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.PaymentChannelCreate;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.PaymentChannelFund;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.SetRegularKey;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.SignerListSet;
import com.cobo.coinlib.coins.XRP.xumm.transcationtype.TrustSet;


import java.util.HashMap;
import java.util.Map;

public class SupportTransactions {
    private static final Map<String,XrpTransaction> sMap;

    static {
        sMap = new HashMap<>();
        register(new AccountSet());
        register(new AccountDelete());
        register(new CheckCancel());
        register(new CheckCash());
        register(new CheckCreate());
        register(new DepositPreauth());
        register(new EscrowCancel());
        register(new EscrowCreate());
        register(new EscrowFinish());
        register(new OfferCancel());
        register(new OfferCreate());
        register(new Payment());
        register(new PaymentChannelClaim());
        register(new PaymentChannelCreate());
        register(new PaymentChannelFund());
        register(new SetRegularKey());
        register(new SignerListSet());
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
