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

import android.os.Bundle;
import android.view.View;

import com.cobo.coinlib.coins.BCH.Bch;
import com.cobo.coinlib.coins.LTC.Ltc;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.ReceiveFragmentBinding;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.WatchWallet;

import java.util.Objects;

import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS_INDEX;
import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_CODE;

public class ReceiveCoinFragment extends BaseFragment<ReceiveFragmentBinding> {
    private int index;
    @Override
    protected int setView() {
        return R.layout.receive_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        String coinCode = data.getString(KEY_COIN_CODE);
        mBinding.setCoinCode(coinCode);
        String address = data.getString(KEY_ADDRESS);
        if (coinCode.equals(Coins.BCH.coinCode())) {
            address = Bch.toCashAddress(address);
        } else if (coinCode.equals(Coins.LTC.coinCode())) {
            address = Ltc.convertAddress(address);
        }
        index = data.getInt(KEY_ADDRESS_INDEX);
        mBinding.setAddress(address);
        mBinding.setAddressName(data.getString(KEY_ADDRESS_NAME));
        mBinding.setPath(data.getString(KEY_ADDRESS_PATH));
        mBinding.qrcode.setData(data.getString(KEY_ADDRESS));
        if (WatchWallet.getWatchWallet(mActivity) == WatchWallet.XRP_TOOLKIT) {
            mBinding.button.setVisibility(View.VISIBLE);
            mBinding.button.setOnClickListener(v -> syncXrp());
        }
    }

    public void syncXrp() {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        navigate(R.id.action_to_syncFragment, bundle);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
