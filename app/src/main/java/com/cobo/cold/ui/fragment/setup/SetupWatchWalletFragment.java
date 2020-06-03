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

import com.cobo.cold.R;
import com.cobo.cold.databinding.SetupWatchWalletBinding;
import com.cobo.cold.viewmodel.SupportedWatchWallet;

import static com.cobo.cold.ui.fragment.Constants.KEY_TITLE;
import static com.cobo.cold.viewmodel.SupportedWatchWallet.getSupportedWatchWallet;

public class SetupWatchWalletFragment extends SetupVaultBaseFragment<SetupWatchWalletBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_watch_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.complete.setOnClickListener(v -> complete());
    }

    private void complete() {
        int navId = 0;
        Bundle data = new Bundle();
        SupportedWatchWallet selectWatchOnlyWallet = getSupportedWatchWallet(mActivity);
        switch (selectWatchOnlyWallet) {
            case ELECTRUM:
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
}
