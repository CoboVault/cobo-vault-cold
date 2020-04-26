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

package com.cobo.cold.ui.fragment.main;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cobo.cold.R;
import com.cobo.cold.databinding.ReceiveFragmentBinding;
import com.cobo.cold.ui.fragment.BaseFragment;

import java.util.Objects;

import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_CODE;

public class ReceiveCoinFragment extends BaseFragment<ReceiveFragmentBinding> {
    @Override
    protected int setView() {
        return R.layout.receive_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        mBinding.setAddress(data.getString(KEY_ADDRESS));
        mBinding.setAddressName(data.getString(KEY_ADDRESS_NAME));
        mBinding.setCoinCode(data.getString(KEY_COIN_CODE));
        mBinding.qrcode.setData(data.getString(KEY_ADDRESS));

        mBinding.amount.setOnEditorActionListener((textView, i, keyEvent) -> {
            String text = textView.getText().toString();
            if (!TextUtils.isEmpty(text)) {
                mBinding.qrcode.setData(data.getString(KEY_ADDRESS) + "?amount=" + textView.getText());
            }
            textView.clearFocus();
            final InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
            }
            return false;
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
