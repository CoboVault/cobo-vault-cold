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

package com.cobo.cold.db;

import android.content.Context;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.Utilities;
import com.cobo.cold.db.entity.AccountEntity;
import com.cobo.cold.db.entity.CoinEntity;

import java.util.List;
import java.util.stream.Collectors;

public class PresetData {

    public static List<CoinEntity> generateCoins(Context context) {
        return Coins.SUPPORTED_COINS.stream()
                .map(coin -> mapToCoinEntity(context, coin))
                .collect(Collectors.toList());
    }

    private static CoinEntity mapToCoinEntity(Context context, Coins.Coin coin) {
        CoinEntity entity = new CoinEntity();
        entity.setCoinId(coin.coinId());
        entity.setName(coin.coinName());
        entity.setCoinCode(coin.coinCode());
        entity.setIndex(coin.coinIndex());
        entity.setBelongTo(Utilities.getCurrentBelongTo(context));
        entity.setAddressCount(0);

        for (String path : coin.accountPaths()) {
            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setHdPath(path);
            entity.addAccount(accountEntity);
        }
        return entity;
    }

    public static Coins.CURVE getCurveByPath(String pubKeyPath) {
        String[] strs = pubKeyPath.split("/");
        int coinIndex;
        if (strs[2].endsWith("'")) {
            coinIndex = Integer.parseInt(strs[2].substring(0, strs[2].length() - 1));
        } else {
            coinIndex = Integer.parseInt(strs[2]);
        }
        return Coins.curveFromCoinCode(Coins.coinCodeOfIndex(coinIndex));
    }
}
