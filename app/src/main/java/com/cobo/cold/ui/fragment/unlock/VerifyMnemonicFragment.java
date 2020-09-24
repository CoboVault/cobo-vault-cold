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

package com.cobo.cold.ui.fragment.unlock;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.VerifyOkBinding;
import com.cobo.cold.ui.fragment.setup.MnemonicInputFragment;
import com.cobo.cold.ui.fragment.setup.PreImportFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.util.Keyboard;
import com.cobo.cold.viewmodel.SetupVaultViewModel;
import com.cobo.cold.viewmodel.SharedDataViewModel;

import org.spongycastle.util.encoders.Hex;

import java.util.Objects;

import iton.slip.secret.Share;
import iton.slip.secret.SharedSecretException;
import iton.slip.secret.words.Mnemonic;

import static com.cobo.cold.ui.fragment.Constants.KEY_TITLE;
import static com.cobo.cold.ui.fragment.setup.PreImportFragment.ACTION;
import static com.cobo.cold.ui.fragment.setup.SetPasswordFragment.MNEMONIC;
import static com.cobo.cold.ui.fragment.setup.SetPasswordFragment.SLIP39_SEED;

public class VerifyMnemonicFragment extends MnemonicInputFragment {

    public static final String TAG = "VerifyMnemonicFragment";
    private String action;
    private boolean isSharding;

    @Override
    protected int setView() {
        return R.layout.mnemonic_input_fragment;
    }

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(mActivity).get(SetupVaultViewModel.class);
        Bundle data = Objects.requireNonNull(getArguments());
        action = data.getString(ACTION);
        int mnemonicCount = viewModel.getMnemonicCount().get();
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        isSharding = viewModel.isShardingMnemonic();
        mBinding.hint.setText(getString(R.string.verify_mnemonic_hint, mnemonicCount));

        if (isSharding) {
            initImportSharding();
        } else {
            mBinding.importMnemonic.setText(R.string.confirm_mnemonic);
        }
        String title = data.getString(KEY_TITLE);
        title = TextUtils.isEmpty(title) ? getString(R.string.verify_mnemonic) : title;
        mBinding.toolbarTitle.setText(title);
        mBinding.table.setMnemonicNumber(mnemonicCount);
        mBinding.importMnemonic.setOnClickListener(this::validateMnemonic);
        addMnemonicChangeCallback();
    }

    private void initImportSharding() {
        mBinding.shardingHint.setText(getString(R.string.sharding_no, viewModel.currentSequence() + 1));
        mBinding.shardingHint.setVisibility(View.VISIBLE);
        mBinding.importMnemonic.setText(R.string.next_sharding);
        mBinding.toolbar.setNavigationIcon(new ColorDrawable(Color.TRANSPARENT));
        mBinding.toolbar.setNavigationOnClickListener(null);
        mBinding.toolbar.inflateMenu(R.menu.cancel);
        mBinding.toolbar.setOnMenuItemClickListener(item -> {
            cancelImportSharding();
            return true;
        });
    }

    private void validateMnemonic(View view) {
        String mnemonic = mBinding.table.getWordsList()
                .stream()
                .map(ObservableField::get)
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("");

        if (!viewModel.validateMnemonic(mnemonic)) {
            Utilities.alert(mActivity, getString(R.string.notice),
                    getString(R.string.invalid_mnemonic_hint),
                    getString(R.string.confirm), null);
            return;
        }
        if (!isSharding) {
            SharedDataViewModel model = ViewModelProviders.of(mActivity).get(SharedDataViewModel.class);
            boolean match = model.verifyMnemonic(mnemonic);
            handleVerifyResult(Bundle.forPair(MNEMONIC, mnemonic), match);
        } else {
            if (viewModel.getShares() == null || viewModel.getShares().size() == 0) {
                int remainCount = 0;
                try {
                    Share share = Mnemonic.INSTANCE.decode(mnemonic);
                    if (share.group_threshold == 1) {
                        remainCount = share.member_threshold - 1;
                    }
                } catch (SharedSecretException e) {
                    e.printStackTrace();
                }
                showDialog(mActivity,getString(R.string.verify_pass),
                        getString(R.string.first_sharding_hint, remainCount),
                        getString(R.string.cancel_import_sharding),
                        getString(R.string.continue_import_sharding),
                        this::cancelImportSharding,
                        () -> addSharding(mnemonic));
            } else {
                addSharding(mnemonic);
            }
        }
    }

    private void handleVerifyResult(Bundle bundle, boolean match) {
        if (match) {
            mBinding.table.getWordsList().clear();
            if (PreImportFragment.ACTION_RESET_PWD.equals(action)) {
                navigate(R.id.action_to_setPasswordFragment, bundle);
            } else {
                ModalDialog dialog = new ModalDialog();
                VerifyOkBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.verify_ok, null, false);
                binding.confirm.setOnClickListener(v -> {
                    dialog.dismiss();
                    popBackStack(R.id.settingFragment, false);
                });
                dialog.setBinding(binding);
                dialog.show(mActivity.getSupportFragmentManager(), "");
            }
        } else {
            Utilities.alert(mActivity, getString(R.string.check_failed),
                    getString(R.string.check_mnemonic_failed),
                    getString(R.string.confirm), () -> popBackStack(R.id.settingFragment, false));
        }
    }

    @Override
    protected void onAllShardsCollect() {
        byte[] seed = viewModel.verifyShardingMnemonic();
        viewModel.resetSharding();
        Bundle bundle = new Bundle();
        if (seed != null) {
            bundle.putString(SLIP39_SEED, Hex.toHexString(seed));
            handleVerifyResult(bundle, true);
        } else {
            handleVerifyResult(bundle, false);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onPause() {
        super.onPause();
        Keyboard.hide(mActivity, getView());
    }
}
