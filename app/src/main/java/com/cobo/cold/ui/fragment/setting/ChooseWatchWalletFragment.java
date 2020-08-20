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

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.SettingItemSelectableBinding;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.ui.common.BaseBindingAdapter;
import com.cobo.cold.viewmodel.WatchWallet;

import java.util.ArrayList;
import java.util.List;

import static com.cobo.cold.ui.fragment.Constants.KEY_TITLE;
import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_ADDRESS_FORMAT;
import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_CHOOSE_WATCH_WALLET;
import static com.cobo.cold.viewmodel.WatchWallet.getWatchWallet;

public class ChooseWatchWalletFragment extends ListPreferenceFragment {

    private Adapter adapter;
    private List<Pair<String,String>> displayItems;
    @Override
    protected void init(View view) {
        Bundle data = getArguments();
        boolean isMainnet = Utilities.isMainNet(mActivity);
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

        displayItems = new ArrayList<>();
        for (int i =0; i < entries.length; i++ ) {
            if (isMainnet || WatchWallet.getWatchWalletById(values[i].toString()).supportTestnet()) {
                displayItems.add(Pair.create(values[i].toString(), entries[i].toString()));
            }
        }

        if (mActivity instanceof SetupVaultActivity) {
            adapter.setItems(displayItems.subList(0, displayItems.size() - 1));
        } else {
            adapter.setItems(displayItems);
        }
        mBinding.list.setAdapter(adapter);
        mBinding.confirm.setText(R.string.next);
        mBinding.confirm.setOnClickListener(v -> next());
        if (mActivity instanceof MainActivity) {
            mBinding.confirm.setVisibility(View.VISIBLE);
        }
    }

    private void next() {
        int navId;
        Bundle data = new Bundle();
        WatchWallet selectWatchOnlyWallet = getWatchWallet(mActivity);
        if (selectWatchOnlyWallet.supportSwitchAccount()) {
            data.putInt(KEY_TITLE, R.string.select_address_format);
            navId = R.id.action_to_selectAddressFormatFragment;
        } else {
            navId = R.id.action_to_export_xpub_guide;
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
        return WatchWallet.COBO.getWalletId();
    }

    @Override
    public void onSelect(int walletId) {
        String old = value;
        value = String.valueOf(walletId);
        if (!old.equals(value)) {
            setWatchWallet();
            adapter.notifyDataSetChanged();
        }
    }

    private void setWatchWallet() {
        if (prefs.edit().putString(SETTING_CHOOSE_WATCH_WALLET, value).commit()) {
            WatchWallet wallet = getWatchWallet(mActivity);
            if (wallet.supportNativeSegwit()) {
                prefs.edit().putString(SETTING_ADDRESS_FORMAT, Coins.Account.SegWit.getType()).apply();
            } else if (wallet.supportNestedSegwit()) {
                prefs.edit().putString(SETTING_ADDRESS_FORMAT, Coins.Account.P2SH.getType()).apply();
            }
        }
    }

    protected class Adapter extends BaseBindingAdapter<Pair<String,String>, SettingItemSelectableBinding> {

        public Adapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.setting_item_selectable;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            SettingItemSelectableBinding binding = DataBindingUtil.getBinding(holder.itemView);
            binding.title.setText(displayItems.get(position).second);
            if (subTitles == null) {
                binding.subTitle.setVisibility(View.GONE);
            } else {
                binding.subTitle.setVisibility(View.VISIBLE);
                binding.subTitle.setText(subTitles[position]);
            }
            binding.setIndex(Integer.parseInt(displayItems.get(position).first));
            binding.setCallback(ChooseWatchWalletFragment.this);
            if (displayItems.get(position).first.equals(value)) {
                binding.setChecked(true);
            } else {
                binding.setChecked(false);
            }
        }

        @Override
        protected void onBindItem(SettingItemSelectableBinding binding, Pair<String,String> item) {
        }
    }
}

