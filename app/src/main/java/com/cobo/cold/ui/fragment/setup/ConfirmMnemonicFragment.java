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

import android.view.View;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.util.Keyboard;
import com.cobo.cold.viewmodel.SetupVaultViewModel;

public class ConfirmMnemonicFragment extends MnemonicInputFragment {

    private int shardingSequence;
    @Override
    protected int setView() {
        return R.layout.mnemonic_input_fragment;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(mActivity).get(SetupVaultViewModel.class);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            Keyboard.hide(mActivity, mBinding.table);
            navigateUp();
        });
        mBinding.toolbarTitle.setText(R.string.confirm_mnemonic);
        mBinding.table.setMnemonicNumber(viewModel.getMnemonicCount().get());

        if (viewModel.isShardingMnemonic()) {
            mBinding.hint.setText(getString(R.string.input_sharding_mnemonic_hint,
                    viewModel.currentSequence() +1));
            shardingSequence = viewModel.currentSequence();
        } else {
            mBinding.hint.setText(getString(R.string.confirm_input_mnemonic_hint,
                    viewModel.getMnemonicCount().get()));
        }

        mBinding.importMnemonic.setText(R.string.confirm);
        mBinding.importMnemonic.setOnClickListener(v -> {
            Keyboard.hide(mActivity, mBinding.importMnemonic);
            verifyMnemonic();
        });
        addMnemonicChangeCallback();
        subscribeVaultState(viewModel.getVaultCreateState());
    }

    protected void navBack() {
        popBackStack(R.id.tabletQrcodeFragment,false);
    }

    private void verifyMnemonic() {
        String mnemonic = mBinding.table.getWordsList()
                .stream()
                .map(ObservableField::get)
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");

        if (viewModel.isShardingMnemonic()) {
            if (mnemonic.equals(viewModel.getShareByIndex(shardingSequence))) {
                if (shardingSequence == viewModel.totalShares() - 1) {
                    viewModel.writeShardingMasterSeed();
                    mBinding.table.getWordsList().clear();
                } else {
                    Utilities.alert(mActivity, getString(R.string.verify_pass),
                            getString(R.string.verify_sharding_pass_hint, shardingSequence + 2),
                            getString(R.string.confirm),() -> {
                                mBinding.table.getWordsList().clear();
                                viewModel.nextSequence();
                                popBackStack(R.id.preCreateShardingFragment,false);
                            });
                }
            } else {
                Utilities.alert(mActivity, getString(R.string.check_failed),
                        getString(R.string.invalid_shard),
                        getString(R.string.confirm), null);
            }
        } else {
            if (mnemonic.equals(viewModel.getMnemonic().getValue())) {
                viewModel.writeMnemonic(mnemonic);
                mBinding.table.getWordsList().clear();
            } else {
                Utilities.alert(mActivity, getString(R.string.hint), getString(R.string.invalid_mnemonic),
                        getString(R.string.confirm), null);
            }
        }

    }
}
