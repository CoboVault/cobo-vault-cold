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

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.db.PresetData;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.util.Keyboard;
import com.cobo.cold.viewmodel.SetupVaultViewModel;

import java.util.List;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;
import static com.cobo.cold.ui.fragment.setup.SetPasswordFragment.handleSeStateAbnormal;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATED;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATING;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATING_FAILED;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_NOT_CREATE;

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

    @Override
    protected void subscribeVaultState(MutableLiveData<Integer> stateLiveData) {
        viewModel.getVaultCreateState().observe(this, state -> {

            if (state == VAULT_STATE_CREATING) {
                showModal();
            } else if (state == VAULT_STATE_CREATED) {
                stateLiveData.setValue(VAULT_STATE_NOT_CREATE);
                viewModel.getVaultCreateState().removeObservers(this);
                Utilities.setVaultCreated(mActivity);
                Utilities.setVaultId(mActivity, viewModel.getVaultId());
                Utilities.setCurrentBelongTo(mActivity, "main");

                Runnable onComplete = () -> {
                    if (dialog != null && dialog.getDialog() != null && dialog.getDialog().isShowing()) {
                        dialog.dismiss();
                    }

                    Bundle data = new Bundle();
                    data.putBoolean(IS_SETUP_VAULT, ((SetupVaultActivity) mActivity).isSetupVault);
                    navigate(R.id.action_add_coin_type1, data);
                };

                List<CoinEntity> coins = PresetData.generateCoins(mActivity);
                viewModel.presetData(coins, onComplete);
            } else if (state == VAULT_STATE_CREATING_FAILED) {
                stateLiveData.setValue(VAULT_STATE_NOT_CREATE);
                viewModel.getVaultCreateState().removeObservers(this);
                if (dialog != null && dialog.getDialog() != null && dialog.getDialog().isShowing()) {
                    dialog.dismiss();
                }
                handleSeStateAbnormal(mActivity);
            }
        });
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
                    mBinding.getRoot().setVisibility(View.INVISIBLE);
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
