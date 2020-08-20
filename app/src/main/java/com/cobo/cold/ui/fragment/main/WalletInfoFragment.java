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

package com.cobo.cold.ui.fragment.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.ExtendPubkeyFormat;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.WalletInfoBinding;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.GlobalViewModel;
import com.cobo.cold.viewmodel.WalletInfoViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import static com.cobo.cold.ui.fragment.Constants.KEY_TITLE;
import static com.cobo.cold.ui.fragment.setup.SelectAddressFormatFragment.KEY_NEED_CONFIRM;
import static com.cobo.cold.viewmodel.WatchWallet.getWatchWallet;

public class WalletInfoFragment extends BaseFragment<WalletInfoBinding> {
    private Coins.Account account;

    @Override
    protected int setView() {
        return R.layout.wallet_info;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.switchAddress.setOnClickListener(v -> switchAddressFormat());
        account = GlobalViewModel.getAccount(mActivity);
        WatchWallet watchWallet = getWatchWallet(mActivity);
        if (!watchWallet.supportSwitchAccount()) {
            mBinding.switchAddress.setVisibility(View.GONE);
        }
        mBinding.addressFormat.setText(getAddressFormat());
        mBinding.addressType.setText(account.getType());
        mBinding.path.setText(account.getPath());

        WalletInfoViewModel viewModel = ViewModelProviders.of(this)
                .get(WalletInfoViewModel.class);

        viewModel.getFingerprint().observe(this, s -> {
            if (!TextUtils.isEmpty(s)) {
                mBinding.fingerprint.setText(s);
            }
        });

        viewModel.getXpub(account).observe(this, xpub -> {
            if (!TextUtils.isEmpty(xpub)) {
                xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub,
                        ExtendPubkeyFormat.valueOf(account.getXpubPrefix()));
                mBinding.xpub.setText(xpub);
            }
        });
    }

    private String getAddressFormat() {
        switch (account) {
            case SegWit:
            case SegWit_TESTNET:
                return getString(R.string.native_segwit);
            case P2PKH:
            case P2PKH_TESTNET:
                return getString(R.string.p2pkh);
            case P2SH:
            case P2SH_TESTNET:
                return getString(R.string.nested_segwit);
        }
        return "";
    }

    private void switchAddressFormat() {
        Bundle data = new Bundle();
        data.putInt(KEY_TITLE, R.string.toggle_address_format);
        data.putBoolean(KEY_NEED_CONFIRM,true);
        navigate(R.id.action_to_selectAddressFormatFragment, data);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
