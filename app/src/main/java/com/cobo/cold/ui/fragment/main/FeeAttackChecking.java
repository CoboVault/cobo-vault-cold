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

package com.cobo.cold.ui.fragment.main;

import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;

public class FeeAttackChecking {

    public static final String KEY_DUPLICATE_TX = "key_duplicate_tx";
    public interface FeeAttackCheckingResult {

        int NORMAL = 1;
        int DUPLICATE_TX = 2;
        int SAME_OUTPUTS = 3;
    }
    private BaseFragment fragment;

    public FeeAttackChecking(BaseFragment fragment) {
        this.fragment = fragment;
    }

    public void showFeeAttackWarning() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(fragment.getHostActivity()), R.layout.common_modal,
                null, false);
        modalDialog.setBinding(binding);
        binding.title.setText(R.string.abnormal_tx);
        binding.close.setVisibility(View.GONE);
        binding.subTitle.setText(R.string.fee_attack_warning);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(v -> modalDialog.dismiss());
        modalDialog.show(fragment.getHostActivity().getSupportFragmentManager(),"");
    }
}
