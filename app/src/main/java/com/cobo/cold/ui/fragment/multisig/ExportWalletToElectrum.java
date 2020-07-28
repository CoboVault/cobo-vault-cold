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
import com.cobo.cold.databinding.ExportWalletToElectrumBinding;

import java.util.Objects;

public class ExportWalletToElectrum extends MultiSigBaseFragment<ExportWalletToElectrumBinding> {
    @Override
    protected int setView() {
        return R.layout.export_wallet_to_electrum;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        View.OnClickListener naviBack = v -> popBackStack(R.id.multisigFragment, false);
        if (data.getBoolean("isImportMultisig")) {
            mBinding.toolbar.setNavigationOnClickListener(naviBack);
        } else {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        }
        mBinding.export.setOnClickListener(v -> export());
        viewModel.getWalletEntity(data.getString("wallet_fingerprint")).observe(this,
                wallet -> mBinding.text2.setText(getString(R.string.export_multisig_wallet_to_electrum_guide,
                wallet.getTotal(),wallet.getThreshold())));
        mBinding.exportLater.setOnClickListener(naviBack);

    }

    private void export() {
        navigate(R.id.action_to_export_multisig_xpub_to_el, getArguments());
    }
}
