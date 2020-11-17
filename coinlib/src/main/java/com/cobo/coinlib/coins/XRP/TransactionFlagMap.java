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

import java.util.*;
import java.util.Map.Entry;

public class TransactionFlagMap {
    private final static List<Flags> _flags = new ArrayList<>();
    private final static List<Flags> _AccountSetflags = new ArrayList<>();
    static {
        // Constraints flags:
        _flags.add(new Flags(TransactionFlag.FullyCanonicalSig, "FullyCanonicalSig","Constraints"));
        _flags.add(new Flags(TransactionFlag.UniversalMask, "UniversalMask", "Constraints"));
        // AccountSet flags:
        _flags.add(new Flags(TransactionFlag.RequireDestTag, "RequireDestTag", "AccountSet"));
        _flags.add(new Flags(TransactionFlag.OptionalDestTag, "OptionalDestTag", "AccountSet"));
        _flags.add(new Flags(TransactionFlag.RequireAuth, "RequireAuth", "AccountSet"));
        _flags.add(new Flags(TransactionFlag.OptionalAuth, "OptionalAuth", "AccountSet"));
        _flags.add(new Flags(TransactionFlag.DisallowXRP, "DisallowXRP", "AccountSet"));
        _flags.add(new Flags(TransactionFlag.AllowXRP, "AllowXRP", "AccountSet"));
        _flags.add(new Flags(TransactionFlag.AccountSetMask, "AccountSetMask", "AccountSet"));
        // OfferCreate flags:
        _flags.add(new Flags(TransactionFlag.Passive, "Passive", "OfferCreate"));
        _flags.add(new Flags(TransactionFlag.ImmediateOrCancel, "ImmediateOrCancel", "OfferCreate"));
        _flags.add(new Flags(TransactionFlag.FillOrKill, "FillOrKill", "OfferCreate"));
        _flags.add(new Flags(TransactionFlag.Sell, "Sell", "OfferCreate"));
        _flags.add(new Flags(TransactionFlag.OfferCreateMask, "OfferCreateMask", "OfferCreate"));
        // Payment flags:
        _flags.add(new Flags(TransactionFlag.NoRippleDirect, "NoRippleDirect", "Payment"));
        _flags.add(new Flags(TransactionFlag.PartialPayment, "PartialPayment", "Payment"));
        _flags.add(new Flags(TransactionFlag.LimitQuality, "LimitQuality", "Payment"));
        _flags.add(new Flags(TransactionFlag.PaymentMask, "PaymentMask", "Payment"));
        // PaymentChannelClaim flags:
        _flags.add(new Flags(TransactionFlag.Renew, "Renew", "PaymentChannelClaim"));
        _flags.add(new Flags(TransactionFlag.Close, "Close", "PaymentChannelClaim"));
        _flags.add(new Flags(TransactionFlag.PaymentChannelClaimMask, "PaymentChannelClaimMask", "PaymentChannelClaim"));
        // TrustSet flags:
        _flags.add(new Flags(TransactionFlag.SetAuth, "SetAuth", "TrustSet"));
        _flags.add(new Flags(TransactionFlag.SetNoRipple, "SetNoRipple", "TrustSet"));
        _flags.add(new Flags(TransactionFlag.ClearNoRipple, "ClearNoRipple", "TrustSet"));
        _flags.add(new Flags(TransactionFlag.SetFreeze, "SetFreeze", "TrustSet"));
        _flags.add(new Flags(TransactionFlag.ClearFreeze, "ClearFreeze", "TrustSet"));
        _flags.add(new Flags(TransactionFlag.TrustSetMask, "TrustSetMask", "TrustSet"));
        // AccountSet SetFlag/ClearFlag values
        _AccountSetflags.add(new Flags(TransactionFlag.asfRequireDest, "asfRequireDest", "AccountSetFlag"));
        _AccountSetflags.add(new Flags(TransactionFlag.asfRequireAuth, "asfRequireAuth", "AccountSetFlag"));
        _AccountSetflags.add(new Flags(TransactionFlag.asfDisallowXRP, "asfDisallowXRP", "AccountSetFlag"));
        _AccountSetflags.add(new Flags(TransactionFlag.asfDisableMaster, "asfDisableMaster", "AccountSetFlag"));
        _AccountSetflags.add(new Flags(TransactionFlag.asfAccountTxnID, "asfAccountTxnID", "AccountSetFlag"));
        _AccountSetflags.add(new Flags(TransactionFlag.asfNoFreeze, "asfNoFreeze", "AccountSetFlag"));
        _AccountSetflags.add(new Flags(TransactionFlag.asfGlobalFreeze, "asfGlobalFreeze", "AccountSetFlag"));
        _AccountSetflags.add(new Flags(TransactionFlag.asfDefaultRipple, "asfDefaultRipple", "AccountSetFlag"));
        _AccountSetflags.add(new Flags(TransactionFlag.asfDepositAuth, "asfDepositAuth", "AccountSetFlag"));
    }

    public final static String getString(long flag, String transactionType) {
       return  _flags.stream().filter(f -> f.transactionType.equals(transactionType))
                .filter(f -> TransactionFlag.hasFlag(flag, f.flag))
                .map(f -> f.flagName)
                .reduce((s1,s2) -> s1 + "," + s2).orElse(null);
    }

    public final static String getAccountSetFlagsString(long flag, String transactionType) {
        return  _AccountSetflags.stream().filter(f -> f.transactionType.equals(transactionType))
                .filter(f -> f.flag == flag)
                .findFirst()
                .map(f -> f.flagName).orElse(null);
    }

    static class Flags {
        long flag;
        String flagName;
        String transactionType;

        public Flags(long flag, String flagName, String transactionType) {
            this.flag = flag;
            this.flagName = flagName;
            this.transactionType = transactionType;
        }
    }
}
