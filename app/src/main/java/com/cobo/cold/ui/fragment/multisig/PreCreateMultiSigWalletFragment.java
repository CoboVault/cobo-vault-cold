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
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.AddAddressBottomSheetBinding;
import com.cobo.cold.databinding.PreCreateMultisigWalletBinding;
import com.cobo.cold.ui.fragment.main.NumberPickerCallback;
import com.cobo.cold.viewmodel.CollectXpubViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.stream.IntStream;

public class PreCreateMultiSigWalletFragment extends MultiSigBaseFragment<PreCreateMultisigWalletBinding>
        implements NumberPickerCallback {

    private int total = 3;
    private int threshold = 2;
    private int accountValue = 0;
    private MultiSig.Account account;
    private MultiSig.Account[] accounts;
    private State state = State.STATE_NONE;

    enum State {
        STATE1,
        STATE2,
        STATE3,
        STATE_NONE
    }
    @Override
    protected int setView() {
        return R.layout.pre_create_multisig_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.total.setOnClickListener(v -> selectKeyNumber());
        mBinding.threshold.setOnClickListener(v -> selectCosignerNumber());
        mBinding.addressType.setOnClickListener(v -> selectAddressType());
        mBinding.confirm.setOnClickListener(v -> onConfirm());
        accounts = Utilities.isMainNet(mActivity) ?
                new MultiSig.Account[] {MultiSig.Account.P2WSH, MultiSig.Account.P2SH_P2WSH, MultiSig.Account.P2SH }
                :
                new MultiSig.Account[] {MultiSig.Account.P2WSH_TEST,MultiSig.Account.P2SH_P2WSH_TEST, MultiSig.Account.P2SH_TEST };
        account = accounts[0];
        updateUI();
    }

    private void onConfirm() {
        CollectXpubViewModel vm = ViewModelProviders.of(mActivity).get(CollectXpubViewModel.class);
        vm.initXpubInfo(total);
        vm.startCollect = false;
        List<CollectXpubViewModel.XpubInfo> info = vm.getXpubInfo();
        for (int i = 1; i <= total; i++) {
            if (i == 1) {
                info.add(new CollectXpubViewModel.XpubInfo(1, viewModel.getXfp(), viewModel.getXpub(account)));
            } else {
                info.add(new CollectXpubViewModel.XpubInfo(i,null,null));
            }
        }
        Bundle data = new Bundle();
        data.putInt("total", total);
        data.putInt("threshold", threshold);
        data.putString("path", account.getPath());
        navigate(R.id.action_to_collect_expubs, data);
    }

    private void updateUI() {
        mBinding.total.setRemindText(String.valueOf(total));
        mBinding.threshold.setRemindText(String.valueOf(threshold));
        mBinding.addressType.setRemindText(viewModel.getAddressTypeString(account));
    }



    private void selectKeyNumber() {
        state = State.STATE1;
        String[] displayValue = IntStream.range(1, 15)
                .mapToObj(i -> String.valueOf(i + 1))
                .toArray(String[]::new);
        showSelector(displayValue, total, 2, 15,getString(R.string.select_key_number));
    }

    private void selectCosignerNumber() {
        state = State.STATE2;
        String[] displayValue = IntStream.range(0, total)
                .mapToObj(i -> String.valueOf(i + 1))
                .toArray(String[]::new);
        showSelector(displayValue, threshold, 1, total, getString(R.string.select_threshold));
    }

    private void selectAddressType() {
        state = State.STATE3;
        String[] displayValue = new String[]{
                getString(R.string.multi_sig_account_segwit),
                getString(R.string.multi_sig_account_p2sh),
                getString(R.string.multi_sig_account_legacy)
                };
        showSelector(displayValue, accountValue, 0,displayValue.length -1, getString(R.string.select_address_type));
    }

    private void showSelector(String[] displayValue, int value, int min, int max,String title) {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        AddAddressBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.add_address_bottom_sheet,null,false);
        binding.setValue(value);
        binding.title.setText(title);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.picker.setDisplayedValues(displayValue);
        binding.picker.setMinValue(min);
        binding.picker.setMaxValue(max);
        binding.picker.setValue(value);
        binding.picker.setOnValueChangedListener((picker, oldVal, newVal) -> binding.setValue(newVal));
        binding.confirm.setText(R.string.confirm);
        binding.confirm.setOnClickListener(v-> {
            onValueSet(binding.picker.getValue());
            dialog.dismiss();

        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    @Override
    public void onValueSet(int value) {
        if (state == State.STATE1) {
            total = value;
        } else if(state == State.STATE2) {
            threshold = value;
        } else if(state == State.STATE3) {
            accountValue = value;
            account = accounts[value];
        }
        threshold = Math.min(threshold, total);
        state = State.STATE_NONE;
        updateUI();
    }
}
