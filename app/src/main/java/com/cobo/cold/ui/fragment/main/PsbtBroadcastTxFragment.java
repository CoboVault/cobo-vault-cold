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
import static com.cobo.cold.viewmodel.WatchWallet.PSBT_MULTISIG_SIGN_ID;

public class PsbtBroadcastTxFragment extends BaseFragment<BroadcastPsbtTxFragmentBinding> {

    public static final String KEY_TXID = "txId";
    private View.OnClickListener goHome;
    private TxEntity txEntity;
    private boolean isMultisig;
    private boolean signed;

    @Override
    protected int setView() {
        return R.layout.broadcast_psbt_tx_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
            this.txEntity = txEntity;
            isMultisig = txEntity.getSignId().equals(PSBT_MULTISIG_SIGN_ID);
            mBinding.setCoinCode(txEntity.getCoinCode());
            mBinding.qrcodeLayout.qrcode.setEncodingScheme(DynamicQrCodeView.EncodingScheme.Bc32);
            mBinding.qrcodeLayout.qrcode.setData(Hex.toHexString(Base64.decode(txEntity.getSignedHex())));
            updateUI();
        });
    }

    private void updateUI() {
        if(isMultisig) {
            goHome = v-> popBackStack(R.id.multisigFragment,false);
            mBinding.toolbarTitle.setText(getString(R.string.export_tx));
            mBinding.qrcodeLayout.hint.setVisibility(View.GONE);
            mBinding.exportToSdcard.setVisibility(View.VISIBLE);
            mBinding.exportToSdcard.setOnClickListener(v ->
                    showExportPsbtDialog(mActivity, txEntity, null));
            mBinding.signStatus.setText(getString(R.string.sign_status)+ ":"+ getSignStatus(txEntity));
            if (signed) {
                mBinding.scanHint.setText(R.string.broadcast_multisig_tx_hint);
            } else {
                mBinding.scanHint.setText(R.string.export_multisig_tx_hint);
            }
        } else {
            goHome = v-> popBackStack(R.id.assetFragment,false);
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
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);
    }

    private String getSignStatus(TxEntity txEntity) {
        String signStatus = txEntity.getSignStatus();

        String[] splits = signStatus.split("-");
        int sigNumber = Integer.parseInt(splits[0]);
        int reqSigNumber = Integer.parseInt(splits[1]);

        String text;
        if (sigNumber == 0) {
            text = getString(R.string.unsigned);
        } else if (sigNumber < reqSigNumber) {
            text = getString(R.string.partial_signed);
        } else {
            text = getString(R.string.signed);
            signed = true;
        }

        return text;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
