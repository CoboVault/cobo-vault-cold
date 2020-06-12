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

import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.databinding.BroadcastBlueTxFragmentBinding;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.views.qrcode.DynamicQrCodeView;
import com.cobo.cold.viewmodel.CoinListViewModel;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.util.Objects;

public class BlueWalletBroadcastTxFragment extends BaseFragment<BroadcastBlueTxFragmentBinding> {

    public static final String KEY_TXID = "txId";
    private View.OnClickListener goHome = v -> popBackStack(R.id.assetFragment,false);

    @Override
    protected int setView() {
        return R.layout.broadcast_blue_tx_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
            mBinding.setCoinCode(txEntity.getCoinCode());
            mBinding.qrcodeLayout.qrcode.setEncodingScheme(DynamicQrCodeView.EncodingScheme.Bc32);
            mBinding.qrcodeLayout.qrcode.setData(Hex.toHexString(Base64.decode(txEntity.getSignedHex())));
        });
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
