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

import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.databinding.SetupSyncBinding;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.SetupVaultViewModel;

import java.util.List;

public class SetupSyncFragment extends SetupVaultBaseFragment<SetupSyncBinding> {

    @Override
    protected int setView() {
        return R.layout.setup_sync;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.complete.setOnClickListener(this::complete);
        SetupVaultViewModel model = ViewModelProviders.of(mActivity).get(SetupVaultViewModel.class);
        subscribe(model.getCoins());
    }

    private void complete(View view) {
        navigate(R.id.action_to_setupCompleteFragment);
    }

    private void subscribe(LiveData<List<CoinEntity>> coins) {
        coins.observe(this, this::generateSyncData);
    }

    private void generateSyncData(List<CoinEntity> coinEntities) {
        ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                .generateSync(coinEntities).observe(this, sync -> {
            if (!TextUtils.isEmpty(sync)) {
                mBinding.sync.qrcodeLayout.qrcode.setData(sync);
            }
        });
    }
}
