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

package com.cobo.cold.ui.fragment.setup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.SyncWatchWalletGuideBinding;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.WatchWallet;

import java.util.Objects;

public class SyncWatchWalletGuide extends BaseFragment<SyncWatchWalletGuideBinding> {

    private WatchWallet watchWallet;
    private String coinCode;

    @Override
    protected int setView() {
        return R.layout.sync_watch_wallet_guide;
    }

    @Override
    protected void init(View view) {
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbarTitle.setText(R.string.sync_watch_wallet);
        coinCode = Objects.requireNonNull(getArguments()).getString("coinCode");
        if (mActivity instanceof MainActivity) {
            mBinding.skip.setOnClickListener( v -> {
                startActivity(new Intent(mActivity, MainActivity.class));
                mActivity.finish();
            });
        } else {
            mBinding.skip.setText(R.string.sync_later);
            mBinding.skip.setOnClickListener(v -> navigate(R.id.action_to_setupCompleteFragment));
        }

        mBinding.text1.setText(getString(getSyncWatchWalletGuideTitle(watchWallet), coinCode));
        mBinding.text2.setText(getString(getSyncWatchWalletGuide(watchWallet),
                Coins.coinNameFromCoinCode(coinCode), coinCode));
        mBinding.export.setText(getButtonText());
        mBinding.export.setOnClickListener(v -> export());
    }

    private void export() {
        switch (watchWallet) {
            case COBO:
            case XUMM:
            case POLKADOT_JS:
                Bundle bundle = getArguments();
                bundle.putBoolean("fromSyncGuide", true);
                navigate(R.id.action_to_syncFragment, bundle);
                break;
        }
    }

    private String getButtonText() {
        int id = 0;
        switch (watchWallet) {
            case COBO:
                id = R.string.sync_cobo_guide_button_text;
                break;
            case XUMM:
                id = R.string.sync_xumm_guide_button_text;
                break;
            case POLKADOT_JS:
                id = R.string.sync_polkadot_js_guide_button_text;
                break;
        }
        return getString(id, coinCode);
    }

    public static int getSyncWatchWalletGuideTitle(WatchWallet watchWallet) {
        switch (watchWallet) {
            case COBO:
                return R.string.sync_cobo_wallet_guide_title;
            case XUMM:
                return R.string.sync_xumm_guide_title;
            case POLKADOT_JS:
                return R.string.sync_polkadotjs_wallet_guide_title;
        }
        return 0;
    }

    public static int getSyncWatchWalletGuide(WatchWallet watchWallet) {
        switch (watchWallet) {
            case COBO:
                return R.string.sync_cobo_wallet_guide_text;
            case XUMM:
                return R.string.sync_xumm_guide_text;
            case POLKADOT_JS:
                return R.string.sync_polkadot_js_guide_text;
        }
        return 0;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
