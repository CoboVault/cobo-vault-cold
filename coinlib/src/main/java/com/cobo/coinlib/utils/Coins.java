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

package com.cobo.coinlib.utils;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Coins {
    public static final Coin BTC = new Coin("bitcoin", "BTC", "Bitcoin", 0,
            new String[] {
                    Account.P2PKH.getPath(),
                    Account.P2SH.getPath(),
                    Account.SegWit.getPath(),
            });

    public static final Coin XTN = new Coin("bitcoin_testnet", "XTN", "Bitcoin TestNet", 1,
            new String[] {
                    Account.P2PKH_TESTNET.getPath(),
                    Account.P2SH_TESTNET.getPath(),
                    Account.SegWit_TESTNET.getPath(),
            });

    public enum Account{
        P2PKH("M/44'/0'/0'","P2PKH"),
        P2SH("M/49'/0'/0'","P2SH-P2WPKH"),
        SegWit("M/84'/0'/0'","P2WPKH"),

        P2PKH_TESTNET("M/44'/1'/0'","P2PKH"),
        P2SH_TESTNET("M/49'/1'/0'","P2SH-P2WPKH"),
        SegWit_TESTNET("M/84'/1'/0'","P2WPKH");

        private String path;
        private String type;

        Account(String path, String type) {
            this.path = path;
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public String getType() {
            return type;
        }

        public static Account ofPath(String path) {
            for (Account value : Account.values()) {
                if (value.getPath().equalsIgnoreCase(path)) {
                    return value;
                }
            }
            return SegWit;
        }

        public String getXpubPrefix() {
            switch (this) {
                case P2PKH:
                    return "xpub";
                case P2SH:
                    return "ypub";
                case SegWit:
                    return "zpub";
                case P2PKH_TESTNET:
                    return "tpub";
                case P2SH_TESTNET:
                    return "upub";
                case SegWit_TESTNET:
                    return "vpub";
            }
            return "xpub";
        }

        public boolean isMainNet() {
            return this == P2PKH || this == P2SH || this == SegWit;
        }
    }

    public static final List<Coin> SUPPORTED_COINS = Arrays.asList(
            BTC,XTN
    );

    public static boolean isCoinSupported(@NonNull String coinCode) {
        return SUPPORTED_COINS.stream().anyMatch(coin -> coin.coinCode.equals(coinCode));
    }

    public static boolean supportMultiSigner(@NonNull String coinCode) {
        return true;
    }

    public static String coinCodeFromCoinId(String coinId) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinId.equals(c.coinId))
                .findFirst();
        return coin.isPresent() ? coin.get().coinCode : "";
    }

    public static String coinIdFromCoinCode(String coinCode) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinCode.equals(c.coinCode))
                .findFirst();
        return coin.isPresent() ? coin.get().coinId : "";
    }

    public static CURVE curveFromCoinCode(String coinCode) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinCode.equals(c.coinCode))
                .findFirst();
        return coin.isPresent() ? coin.get().curve : CURVE.SECP256K1;
    }

    public static String coinCodeOfIndex(int coinIndex) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinIndex == c.coinIndex)
                .findFirst();
        return coin.isPresent() ? coin.get().coinCode : "";
    }

    public static String coinNameOfCoinId(String coinId) {
        Optional<Coin> coin = SUPPORTED_COINS.stream()
                .filter(c -> coinId.equals(c.coinId))
                .findFirst();
        return coin.isPresent() ? coin.get().coinName() : "";
    }

    public static boolean showPublicKey(String coinCode) {
        return false;
    }

    public enum CURVE {
        ED25519,
        SECP256K1,
        SECP256R1
    }

    public static class Coin {
        private final String coinId;
        private final String coinCode;
        private final String coinName;
        private final int coinIndex;
        private final CURVE curve;
        private final String[] accountPaths;


        public Coin(String coinId, String coinCode, String coinName, int coinIndex, String[] accountPaths) {
            this(coinId, coinCode, coinName, coinIndex, accountPaths, CURVE.SECP256K1);
        }

        public Coin(String coinId, String coinCode, String coinName, int coinIndex, String[] accountPaths, CURVE curve) {
            this.coinId = coinId;
            this.coinCode = coinCode;
            this.coinName = coinName;
            this.coinIndex = coinIndex;
            this.curve = curve;
            this.accountPaths = accountPaths;
        }

        public String coinId() {
            return coinId;
        }

        public String coinCode() {
            return coinCode;
        }

        public String coinName() {
            return coinName;
        }

        public int coinIndex() {
            return coinIndex;
        }

        public CURVE curve() {
            return curve;
        }

        public String[] accountPaths() {
            return accountPaths;
        }
    }

}
