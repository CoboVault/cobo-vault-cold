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

package com.cobo.cold.viewmodel;

import android.content.Context;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;

import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_CHOOSE_WATCH_WALLET;

public enum WatchWallet {
    COBO("0"),
    ELECTRUM("1"),
    WASABI("2"),
    BLUE("3"),
    GENERIC("4");

    private String walletId;
    WatchWallet(String walletId) {
        this.walletId = walletId;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getWalletName(Context context) {
        return context.getResources()
                .getStringArray(R.array.watch_wallet_list)[Integer.parseInt(walletId)];
    }

    public static WatchWallet getWatchWallet(Context context) {
        String wallet = Utilities.getPrefs(context)
                .getString(SETTING_CHOOSE_WATCH_WALLET, ELECTRUM.getWalletId());
        WatchWallet selectWatchWallet = ELECTRUM;
        for (WatchWallet watchWallet: WatchWallet.values()) {
            if (watchWallet.getWalletId().equals(wallet)) {
                selectWatchWallet = watchWallet;
                break;
            }
        }
        return selectWatchWallet;
    }

    public boolean supportPsbt() {
        switch (this) {
            case GENERIC:
            case BLUE:
            case WASABI:
                return true;
                default:return false;
        }
    }

    public boolean supportBc32QrCode() {
        switch (this) {
            case GENERIC:
            case BLUE:
                return true;
            default:return false;
        }
    }

    public boolean supportQrCode() {
        switch (this) {
            case ELECTRUM:
            case COBO:
            case GENERIC:
            case BLUE:
                return true;
            default:return false;
        }
    }

    public boolean supportSdcard() {
        switch (this) {
            case ELECTRUM:
            case GENERIC:
            case WASABI:
                return true;
            default:return false;
        }
    }
}