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

import android.view.View;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.fragment.setting.ListPreferenceFragment;

import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_ADDRESS_FORMAT;

public class SelectAddressFormatFragment extends ListPreferenceFragment {

    @Override
    protected void init(View view) {
        super.init(view);
        subTitles = getResources().getStringArray(R.array.address_format_subtitle);
        mBinding.confirm.setVisibility(View.VISIBLE);
        mBinding.confirm.setText(R.string.next);
        mBinding.confirm.setOnClickListener(v -> next());
        if (mActivity instanceof MainActivity) {
            mBinding.confirm.setVisibility(View.GONE);
        }
    }

    private void next() {
        navigate(R.id.action_to_export_xpub_guide);
    }

    @Override
    protected int getEntries() {
        return R.array.address_format;
    }

    @Override
    protected int getValues() {
        return R.array.address_format_value;
    }

    @Override
    protected String getKey() {
        return SETTING_ADDRESS_FORMAT;
    }

    @Override
    protected String defaultValue() {
        return Coins.Account.P2SH.getType();
    }

    @Override
    public void onSelect(int position) {
        String old = value;
        value = values[position].toString();
        if (!old.equals(value)) {
            prefs.edit().putString(getKey(), value).apply();
            adapter.notifyDataSetChanged();
        }
    }
}
