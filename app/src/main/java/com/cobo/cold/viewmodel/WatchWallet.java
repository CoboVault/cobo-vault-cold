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

package com.cobo.cold.viewmodel;

import android.content.Context;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;

import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_CHOOSE_WATCH_WALLET;

public enum WatchWallet {
    COBO("0"),
    POLKADOT_JS("1"),
    XRP_TOOLKIT("2");

    public static final String XRP_TOOLKIT_SIGN_ID = "xrp_toolkit_sign_id";
    public static final String POLKADOT_JS_SIGN_ID = "polkadot_js_sign_id";

    private final String walletId;
    WatchWallet(String walletId) {
        this.walletId = walletId;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getWalletName(Context context) {
        String[] wallets = context.getResources().getStringArray(R.array.watch_wallet_list);
        if (walletId.equals(COBO.walletId)) return wallets[0];
        else if (walletId.equals(XRP_TOOLKIT.walletId)) return wallets[1];
        return wallets[0];
    }

    public Coins.Coin[] getSupportedCoins() {
        switch (this) {
            case COBO:
                return Coins.SUPPORTED_COINS.toArray(new Coins.Coin[0]);
            case POLKADOT_JS:
                return new Coins.Coin[] {Coins.DOT, Coins.KSM };
            case XRP_TOOLKIT:
                return new Coins.Coin[] { Coins.XRP };
        }
        return null;
    }

    public String getSignId() {
        switch (this) {
            case POLKADOT_JS:
                return POLKADOT_JS_SIGN_ID;
            case XRP_TOOLKIT:
                return XRP_TOOLKIT_SIGN_ID;
        }
        return null;
    }

    public static WatchWallet getWatchWallet(Context context) {
        String wallet = Utilities.getPrefs(context)
                .getString(SETTING_CHOOSE_WATCH_WALLET, COBO.getWalletId());
        return getWatchWalletById(wallet);
    }

    public static WatchWallet getWatchWalletById(String walletId) {
        WatchWallet selectWatchWallet = COBO;
        for (WatchWallet watchWallet: WatchWallet.values()) {
            if (watchWallet.getWalletId().equals(walletId)) {
                selectWatchWallet = watchWallet;
                break;
            }
        }
        return selectWatchWallet;
    }
}