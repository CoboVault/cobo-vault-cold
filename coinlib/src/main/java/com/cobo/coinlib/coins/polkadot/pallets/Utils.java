package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.UOS.Network;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Utils {
    public static String getReadableBalanceString(Network network, BigInteger amount) {
        return new BigDecimal(amount)
                .divide(BigDecimal.TEN.pow(network.decimals), Math.min(network.decimals, 8), BigDecimal.ROUND_HALF_UP)
                .stripTrailingZeros().toPlainString();
    }
}
