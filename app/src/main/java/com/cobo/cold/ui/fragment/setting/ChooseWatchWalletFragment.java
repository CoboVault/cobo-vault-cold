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

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.viewmodel.WatchWallet;

import java.util.Arrays;

import static com.cobo.cold.ui.fragment.Constants.KEY_TITLE;
import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_ADDRESS_FORMAT;
import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_CHOOSE_WATCH_WALLET;
import static com.cobo.cold.viewmodel.WatchWallet.getWatchWallet;

public class ChooseWatchWalletFragment extends ListPreferenceFragment {

    @Override
    protected void init(View view) {
        Bundle data = getArguments();
        if (data != null) {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
            mBinding.toolbarTitle.setText(data.getInt(KEY_TITLE));
        } else {
            mBinding.toolbar.setVisibility(View.GONE);
        }
        prefs = Utilities.getPrefs(mActivity);
        entries = getResources().getStringArray(getEntries());
        values = getResources().getStringArray(getValues());
        value = prefs.getString(getKey(), defaultValue());
        adapter = new Adapter(mActivity);
        if (mActivity instanceof SetupVaultActivity) {
            adapter.setItems(Arrays.asList(Arrays.copyOfRange(entries,0,entries.length - 1)));
        } else {
            adapter.setItems(Arrays.asList(entries));
        }
        mBinding.list.setAdapter(adapter);
        mBinding.confirm.setText(R.string.next);
        mBinding.confirm.setOnClickListener(v -> next());
        if (mActivity instanceof MainActivity) {
            mBinding.confirm.setVisibility(View.VISIBLE);
        }
    }

    private void next() {
        int navId = 0;
        Bundle data = new Bundle();
        WatchWallet selectWatchOnlyWallet = getWatchWallet(mActivity);
        switch (selectWatchOnlyWallet) {
            case ELECTRUM:
            case GENERIC:
                data.putInt(KEY_TITLE, R.string.select_address_format);
                navId = R.id.action_to_selectAddressFormatFragment;
                break;
            case COBO:
            case WASABI:
            case BLUE:
                navId = R.id.action_to_export_xpub_guide;
                break;

        }
        navigate(navId, data);
    }

    @Override
    protected int getEntries() {
        return R.array.watch_wallet_list;
    }

    @Override
    protected int getValues() {
        return R.array.watch_wallet_list_values;
    }

    @Override
    protected String getKey() {
        return SETTING_CHOOSE_WATCH_WALLET;
    }

    @Override
    protected String defaultValue() {
        return WatchWallet.ELECTRUM.getWalletId();
    }

    @Override
    public void onSelect(int position) {
        String old = value;
        value = values[position].toString();
        if (!old.equals(value)) {
            setWatchWallet();
            adapter.notifyDataSetChanged();
        }
    }

    private void setWatchWallet() {
        prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, value).apply();
        if (value.equals(WatchWallet.COBO.getWalletId())) {
            prefs.edit().putString(SETTING_ADDRESS_FORMAT, Coins.Account.P2SH.getType()).apply();
        } else if (value.equals(WatchWallet.WASABI.getWalletId())
                || value.equals(WatchWallet.BLUE.getWalletId())) {
            prefs.edit().putString(SETTING_ADDRESS_FORMAT, Coins.Account.SegWit.getType()).apply();
        }
    }
}

