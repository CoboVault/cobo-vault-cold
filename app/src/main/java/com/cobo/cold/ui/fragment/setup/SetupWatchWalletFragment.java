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

import android.os.Bundle;
import android.view.View;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.SetupWatchWalletBinding;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.viewmodel.WatchWallet;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;

public class SetupWatchWalletFragment extends SetupVaultBaseFragment<SetupWatchWalletBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_watch_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.complete.setOnClickListener(v -> onConfirm());
    }

    private void onConfirm() {
        WatchWallet watchWallet = WatchWallet.getWatchWallet(mActivity);
        Bundle data = new Bundle();
        data.putBoolean(IS_SETUP_VAULT, ((SetupVaultActivity) mActivity).isSetupVault);
        switch (watchWallet) {
            case COBO:
                navigate(R.id.action_to_manageCoinFragment, data);
                break;
            case XRP_TOOLKIT:
                navigate(R.id.action_to_syncWatchWalletGuide, data);
                break;
            case POLKADOT_JS:
                data.putString("coinCode", Coins.DOT.coinCode());
                navigate(R.id.action_to_manageCoinFragment, data);
                break;
        }
    }
}
