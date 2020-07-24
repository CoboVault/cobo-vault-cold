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
import com.cobo.cold.databinding.BroadcastPsbtTxFragmentBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.views.qrcode.DynamicQrCodeView;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.util.Objects;

import static com.cobo.cold.ui.fragment.main.PsbtTxConfirmFragment.showExportPsbtDialog;

public class PsbtBroadcastTxFragment extends BaseFragment<BroadcastPsbtTxFragmentBinding> {

    public static final String KEY_TXID = "txId";
    private View.OnClickListener goHome = v -> popBackStack(R.id.assetFragment,false);
    private TxEntity txEntity;

    @Override
    protected int setView() {
        return R.layout.broadcast_psbt_tx_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            mBinding.setCoinCode(txEntity.getCoinCode());
            mBinding.qrcodeLayout.qrcode.setEncodingScheme(DynamicQrCodeView.EncodingScheme.Bc32);
            mBinding.qrcodeLayout.qrcode.setData(Hex.toHexString(Base64.decode(txEntity.getSignedHex())));
        });

        WatchWallet wallet = WatchWallet.getWatchWallet(mActivity);

        if (wallet.supportSdcard()) {
            mBinding.qrcodeLayout.hint.setVisibility(View.GONE);
            mBinding.exportToSdcard.setVisibility(View.VISIBLE);
            mBinding.exportToSdcard.setOnClickListener(v ->
                    showExportPsbtDialog(mActivity, txEntity, null));
        } else {
            mBinding.exportToSdcard.setVisibility(View.GONE);
        }
        mBinding.scanHint.setText(getString(R.string.use_wallet_to_broadcast,
                WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
    }


    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
