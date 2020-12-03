/*
 *
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
 *
 */

package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.view.View;

import com.cobo.cold.R;
import com.cobo.cold.databinding.MultisigWalletBinding;

import java.util.Objects;

public class WalletFragment extends MultiSigBaseFragment<MultisigWalletBinding>
        implements ClickHandler {
    @Override
    protected int setView() {
        return R.layout.multisig_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.setClickHandler(this);
        if ("Caravan".equalsIgnoreCase(Objects.requireNonNull(getArguments()).getString("creator"))) {
            mBinding.exportWalletToCosigner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(int id) {
        Bundle data = getArguments();
        Objects.requireNonNull(data).putBoolean("setup",false);
        data.putBoolean("multisig",true);
        navigate(id, data);
    }

}

