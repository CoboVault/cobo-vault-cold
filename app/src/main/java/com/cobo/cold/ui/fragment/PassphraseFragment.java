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

package com.cobo.cold.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.navigation.Navigation;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.ConfirmModalBinding;
import com.cobo.cold.databinding.CreateVaultModalBinding;
import com.cobo.cold.databinding.PassphraseBinding;
import com.cobo.cold.db.PresetData;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.ui.fragment.setup.SetupVaultBaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.util.Keyboard;
import com.cobo.cold.viewmodel.SetupVaultViewModel;

import java.util.List;
import java.util.Objects;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;
import static com.cobo.cold.Utilities.IS_SET_PASSPHRASE;
import static com.cobo.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATED;
import static com.cobo.cold.viewmodel.SetupVaultViewModel.VAULT_STATE_CREATING;

public class PassphraseFragment extends SetupVaultBaseFragment<PassphraseBinding> {

    private static final String SPACE = " ";
    private static final int MAX_LENGTH = 128;
    private final ObservableField<String> passphrase = new ObservableField<>("");
    private ModalDialog dialog;

    @Override
    protected int setView() {
        return R.layout.passphrase;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            if (mActivity instanceof SetupVaultActivity) {
                mActivity.finish();
            } else {
                Keyboard.hide(mActivity, mBinding.input);
                navigateUp();
            }
        });
        mBinding.setPassphrase(passphrase);
        setFilterSpace(mBinding.input);
        updateShowHide();
        mBinding.eye.setOnClickListener(v -> updateShowHide());
        mBinding.confirm.setOnClickListener(v -> confirmInput());
        mBinding.input.setShowSoftInputOnFocus(false);
        mBinding.input.setOnClickListener(v -> Keyboard.show(mActivity, mBinding.input));
    }

    private void confirmInput() {
        ModalDialog dialog = new ModalDialog();
        ConfirmModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.confirm_modal, null, false);
        binding.text.setText(R.string.passphrase_confirm_modal_title);
        binding.text2.setText(R.string.passphrase_confirm_modal_hint);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setText(R.string.confirm);
        dialog.setBinding(binding);
        binding.confirm.setOnClickListener(v -> {
            Keyboard.hide(mActivity, mBinding.input);
            dialog.dismiss();
            updatePassphrase();
        });
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void updatePassphrase() {
        viewModel.setPassword(getArguments().getString(PASSWORD));
        viewModel.updatePassphrase(passphrase.get());
        subscribeVaultState(viewModel);
    }

    private void updateShowHide() {
        mBinding.input.setTransformationMethod(mBinding.eye.isChecked() ?
                null
                :
                PasswordTransformationMethod.getInstance());
        mBinding.input.setSelection(Objects.requireNonNull(passphrase.get()).length());
    }

    private void setFilterSpace(EditText editText) {
        InputFilter letterFilter = (source, start, end, dest, dstart, dend) -> {
            if (SPACE.equals(source.toString())) {
                return "";
            } else {
                return null;
            }
        };

        InputFilter lengthFilter = new InputFilter.LengthFilter(MAX_LENGTH);
        editText.setFilters(new InputFilter[]{letterFilter, lengthFilter});
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void subscribeVaultState(SetupVaultViewModel viewModel) {
        viewModel.getVaultCreateState().observe(this, state -> {
            if (state == VAULT_STATE_CREATING) {
                showModal();
            } else if (state == VAULT_STATE_CREATED) {
                Utilities.setVaultCreated(mActivity);
                Utilities.setVaultId(mActivity, viewModel.getVaultId());
                Utilities.setCurrentBelongTo(mActivity,
                        TextUtils.isEmpty(passphrase.get()) ? "main" : "hidden");

                Runnable onComplete = () -> {
                    Bundle data = new Bundle();
                    data.putBoolean(IS_SETUP_VAULT, false);
                    data.putBoolean(IS_SET_PASSPHRASE, true);
                    if (dialog != null && dialog.getDialog() != null
                            && dialog.getDialog().isShowing()) {
                        dialog.dismiss();
                    }
                    if (TextUtils.isEmpty(passphrase.get())) {
                        startActivity(new Intent(mActivity, MainActivity.class));
                    } else {
                        Navigation.findNavController(mActivity, R.id.nav_host_fragment)
                                .navigate(R.id.action_to_manageCoinFragment, data);
                    }
                };
                List<CoinEntity> coins = PresetData.generateCoins(mActivity);
                viewModel.presetData(coins, onComplete);

            }
        });
    }

    private void showModal() {
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
                    System.out.println("exception...");
                }
            }
        };
        handler.post(runnable);
    }
}
