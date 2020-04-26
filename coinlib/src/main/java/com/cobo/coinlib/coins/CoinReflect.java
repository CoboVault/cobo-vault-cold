/*
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cobo.coinlib.coins;

import androidx.annotation.NonNull;

import java.util.Objects;

class CoinReflect {
    static String getCoinClassByCoinCode(@NonNull String coinCode) {
        //return the specific class name of a coinCode,eg:com.cobo.coinlib.coins.BTC.Btc
        return getPackageName() + "." + coinCode + "." + toUpperFirstChar(coinCode);
    }

    private static String getPackageName() {
        return Objects.requireNonNull(CoinReflect.class.getPackage()).getName();
    }

    private static String toUpperFirstChar(String string) {
        char[] charArray = string.toLowerCase().toCharArray();
        charArray[0] -= 32;
        return String.valueOf(charArray);
    }
}
