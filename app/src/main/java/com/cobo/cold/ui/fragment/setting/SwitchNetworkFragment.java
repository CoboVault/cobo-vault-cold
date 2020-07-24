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

import android.content.Intent;

import com.cobo.cold.R;
import com.cobo.cold.ui.MainActivity;

import static com.cobo.cold.Utilities.NET_MDOE;

public class SwitchNetworkFragment extends ListPreferenceFragment {

    @Override
    protected int getEntries() {
        return R.array.network_entries;
    }

    @Override
    protected int getValues() {
        return R.array.network_values;
    }

    @Override
    protected String getKey() {
        return NET_MDOE;
    }

    @Override
    protected String defaultValue() {
        return "mainnet";
    }

    @Override
    public void onSelect(int position) {
        String old = value;
        value = values[position].toString();
        if (!old.equals(value)) {
            prefs.edit().putString(NET_MDOE, value).apply();
            adapter.notifyDataSetChanged();
            onNetWorkSwitch();
        }
    }

    private void onNetWorkSwitch() {
        startActivity(new Intent(mActivity, MainActivity.class));
    }
}

