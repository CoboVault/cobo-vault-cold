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

package com.cobo.cold.ui.fragment.setting;

import android.content.Context;
import android.hardware.fingerprint.Fingerprint;
import android.os.Bundle;
import android.view.View;

import com.cobo.cold.R;
import com.cobo.cold.databinding.FingerprintSettingBinding;
import com.cobo.cold.databinding.SettingItemWithArrowBinding;
import com.cobo.cold.fingerprint.FingerprintKit;
import com.cobo.cold.ui.common.BaseBindingAdapter;
import com.cobo.cold.ui.fragment.BaseFragment;

import java.util.List;

public class FingerprintSettingFragment extends BaseFragment<FingerprintSettingBinding> {

    private ListAdapter adapter;
    private FingerprintKit fingerprint;

    @Override
    protected int setView() {
        return R.layout.fingerprint_setting;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        fingerprint = new FingerprintKit(mActivity);
        adapter = new ListAdapter(mActivity);
        refreshList();
        mBinding.add.setOnClickListener(v ->
                navigate(R.id.action_to_fingerprintEnrollFragment, getArguments()));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void refreshList() {
        List<Fingerprint> fingerprints = fingerprint.getEnrolledFingerprints();
        mBinding.add.setVisibility(fingerprints.size() >= 5 ? View.GONE : View.VISIBLE);
        adapter.setItems(fingerprints);
        mBinding.list.setAdapter(adapter);
        mBinding.list.setHasFixedSize(true);
    }

    class ListAdapter extends BaseBindingAdapter<Fingerprint, SettingItemWithArrowBinding> {
        ListAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.setting_item_with_arrow;
        }

        @Override
        protected void onBindItem(SettingItemWithArrowBinding binding, Fingerprint item) {
            binding.title.setText(item.getName());
            Bundle data = new Bundle();
            data.putParcelable("xfp", item);

            binding.getRoot().setOnClickListener(v -> navigate(R.id.action_to_fingerprintManage, data));
        }
    }
}


