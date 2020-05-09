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
import android.view.Gravity;
import android.view.View;

import androidx.databinding.ObservableField;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.db.PresetData;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.util.Keyboard;
import com.cobo.cold.viewmodel.SetupVaultViewModel;

import java.util.List;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATED;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATING;

public class ConfirmMnemonicFragment extends MnemonicInputFragment {

    @Override
    protected int setView() {
        return R.layout.mnemonic_input_fragment;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbarTitle.setText(R.string.confirm_mnemonic);
        mBinding.hint.setText(getString(R.string.confirm_input_mnemonic_hint,
                viewModel.getMnemonicCount().get()));
        mBinding.hint.setGravity(Gravity.CENTER);
        mBinding.importMnemonic.setText(R.string.complete);
        mBinding.importMnemonic.setOnClickListener(v -> {
            Keyboard.hide(mActivity, mBinding.importMnemonic);
            verifyMnemonic();
        });
    }

    @Override
    protected void subscribeVaultState(SetupVaultViewModel viewModel) {
        viewModel.getVaultCreateState().observe(this, state -> {

            if (state == VAULT_STATE_CREATING) {
                showModal();
            } else if (state == VAULT_STATE_CREATED) {
                Utilities.setVaultCreated(mActivity);
                Utilities.setVaultId(mActivity, viewModel.getVaultId());
                Utilities.setCurrentBelongTo(mActivity, "main");
                Utilities.setMnemonicCount(mActivity, viewModel.getMnemonicCount().get());

                Runnable onComplete = () -> {
                    if (dialog != null && dialog.getDialog() != null && dialog.getDialog().isShowing()) {
                        dialog.dismiss();
                    }

                    Bundle data = new Bundle();
                    data.putBoolean(IS_SETUP_VAULT, ((SetupVaultActivity) mActivity).isSetupVault);
                    navigate(R.id.action_to_setupSyncFragment, data);
                };

                List<CoinEntity> coins = PresetData.generateCoins(mActivity);
                viewModel.presetData(coins, onComplete);
            }
        });
    }

    private void verifyMnemonic() {
        String mnemonic = mBinding.table.getWordsList()
                .stream()
                .map(ObservableField::get)
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");
        if (mnemonic.equals(viewModel.getRandomMnemonic().getValue())) {
            viewModel.writeMnemonic(mnemonic);
            mBinding.table.getWordsList().clear();
        } else {
            Utilities.alert(mActivity, getString(R.string.hint), getString(R.string.invalid_mnemonic),
                    getString(R.string.confirm), null);
        }

    }
}
