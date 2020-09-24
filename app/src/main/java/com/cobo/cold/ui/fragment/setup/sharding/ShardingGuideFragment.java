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

package com.cobo.cold.ui.fragment.setup.sharding;

import android.os.Bundle;
import android.view.View;

import com.cobo.cold.MainApplication;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.ShardingGuideBinding;
import com.cobo.cold.ui.fragment.setup.SetupVaultBaseFragment;

import java.util.Objects;

import static com.cobo.cold.setting.LanguageHelper.SIMPLIFIED_CHINESE;
import static com.cobo.cold.ui.fragment.setting.SystemPreferenceFragment.SETTING_LANGUAGE;

public class ShardingGuideFragment extends SetupVaultBaseFragment<ShardingGuideBinding> {

    private int total = 5;
    private int threshold = 3;

    @Override
    protected int setView() {
        return R.layout.sharding_guide;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle data = Objects.requireNonNull(getArguments());
        total = data.getInt("total");
        threshold = data.getInt("threshold");
        String language = Utilities.getPrefs(MainApplication.getApplication())
                .getString(SETTING_LANGUAGE, "zh_rCN");
        if (language.equals(SIMPLIFIED_CHINESE)) {
            mBinding.guide.setText(getString(R.string.shading_guide, total, threshold));
        } else {
            mBinding.guide.setText(getString(R.string.shading_guide, threshold, total));
        }
        mBinding.confirm.setOnClickListener(v-> {
            viewModel.generateSlip39Mnemonic(threshold,total);
            navigate(R.id.action_to_preCreateShardingFragment);
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
