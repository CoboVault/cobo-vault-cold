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
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;

import com.cobo.coinlib.WordList;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.CreateVaultModalBinding;
import com.cobo.cold.databinding.MnemonicInputFragmentBinding;
import com.cobo.cold.db.PresetData;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.util.Keyboard;
import com.cobo.cold.viewmodel.SetupVaultViewModel;

import java.util.Arrays;
import java.util.List;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATED;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATING;

public class MnemonicInputFragment extends SetupVaultBaseFragment<MnemonicInputFragmentBinding> {

    protected ModalDialog dialog;

    @Override
    protected int setView() {
        return R.layout.mnemonic_input_fragment;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.setViewModel(viewModel);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.table.setMnemonicNumber(viewModel.getMnemonicCount().get());
        mBinding.importMnemonic.setOnClickListener(v -> {
            Keyboard.hide(mActivity, mBinding.importMnemonic);
            validateMnemonic(v);
        });
        mBinding.table.getWordsList().forEach(o -> o.addOnPropertyChangedCallback(
                new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(Observable sender, int propertyId) {
                        if (mBinding.table.getWordsList()
                                .stream()
                                .allMatch(s -> isValidWord(s.get()))) {
                            mBinding.importMnemonic.setEnabled(true);
                        } else {
                            mBinding.importMnemonic.setEnabled(false);
                        }
                    }
                }));
        subscribeVaultState(viewModel);
    }

    public static boolean isValidWord(String s) {
        return !TextUtils.isEmpty(s) && Arrays.asList(WordList.words).indexOf(s) != -1;
    }

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
                    navigate(R.id.action_add_coin_type2, data);
                };

                List<CoinEntity> coins = PresetData.generateCoins(mActivity);
                viewModel.presetData(coins, onComplete);
            }
        });
    }

    private void validateMnemonic(View view) {
        String mnemonic = mBinding.table.getWordsList()
                .stream()
                .map(ObservableField::get)
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");


        if (viewModel.validateMnemonic(mnemonic)) {
            viewModel.writeMnemonic(mnemonic);
            mBinding.table.getWordsList().clear();
        } else {
            Utilities.alert(mActivity,
                    getString(R.string.hint),
                    getString(R.string.wrong_mnemonic_please_check),
                    getString(R.string.confirm), null);
        }
    }


    protected void showModal() {
        CreateVaultModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.create_vault_modal, null, false);
        dialog = ModalDialog.newInstance();
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
        String[] steps = mActivity.getResources().getStringArray(R.array.create_vault_step);
        binding.step.setText(steps[0]);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                try {
                    handler.postDelayed(this, 8000);
                    binding.step.setText(steps[i]);
                    i++;
                    if (i > 4) {
                        handler.removeCallbacks(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        handler.post(runnable);
    }
}
