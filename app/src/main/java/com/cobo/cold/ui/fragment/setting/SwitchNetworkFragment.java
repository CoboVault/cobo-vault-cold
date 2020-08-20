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

package com.cobo.cold.ui.fragment.setting;

import android.os.Bundle;
import android.view.View;

import androidx.navigation.Navigation;

import com.cobo.cold.R;
import com.cobo.cold.ui.fragment.Constants;
import com.cobo.cold.viewmodel.WatchWallet;

import java.util.Objects;

import static com.cobo.cold.Utilities.NET_MDOE;
import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_CHOOSE_WATCH_WALLET;

public class SwitchNetworkFragment extends ListPreferenceFragment {

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.confirm.setText(R.string.confirm);
        mBinding.confirm.setVisibility(View.VISIBLE);
        mBinding.confirm.setOnClickListener(v-> {
            Bundle data = new Bundle();
            data.putInt(Constants.KEY_TITLE, R.string.choose_watch_only_wallet);
            Navigation.findNavController(Objects.requireNonNull(getView()))
                    .navigate(R.id.action_to_chooseWatchOnly, data);
        });
    }

    @Override
    protected int getEntries() {
        return R.array.network_entries;
    }

    @Override
    protected int getValues() {
        return R.array.network_values;
    }

    @Override
    protected String getKey() {
        return NET_MDOE;
    }

    @Override
    protected String defaultValue() {
        return "mainnet";
    }

    @Override
    public void onSelect(int position) {
        String old = value;
        value = values[position].toString();
        if (!old.equals(value)) {
            prefs.edit().putString(NET_MDOE, value).apply();
            adapter.notifyDataSetChanged();
            onNetWorkSwitch();
        }
    }

    private void onNetWorkSwitch() {
        WatchWallet wallet = WatchWallet.getWatchWallet(mActivity);
        if ("testnet".equals(value) && !wallet.supportTestnet()) {
            prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, WatchWallet.ELECTRUM.getWalletId()).apply();
        }
        //startActivity(new Intent(mActivity, MainActivity.class));
    }
}

