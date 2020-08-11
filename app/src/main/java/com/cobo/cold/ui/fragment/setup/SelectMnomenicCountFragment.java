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

import com.cobo.cold.R;
import com.cobo.cold.databinding.SelectMnemonicCountBinding;

import static com.cobo.cold.ui.fragment.Constants.KEY_TITLE;

public class SelectMnomenicCountFragment extends SetupVaultBaseFragment<SelectMnemonicCountBinding> {

    private boolean checkMnemonic;

    @Override
    protected int setView() {
        return R.layout.select_mnemonic_count;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.setViewModel(viewModel);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.next.setOnClickListener(this::next);
        Bundle bundle = getArguments();
        if (bundle != null) {
            checkMnemonic = bundle.getBoolean("checkMnemonic");

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkMnemonic) {
            viewModel.setMnemonicCount(24);
        }
    }

    private void next(View view) {
        if (checkMnemonic) {
            getArguments().putString(KEY_TITLE, getString(R.string.check_mnemonic));
            getArguments().putInt("mnemonicCount", viewModel.getMnemonicCount().get());
            navigate(R.id.action_to_verifyMnemonic, getArguments());
        } else {
            navigate(R.id.action_to_mnemonicInputFragment);
        }

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
