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

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.fragment.main.electrum.SignedTxFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.ui.views.qrcode.DynamicQrCodeView;
import com.cobo.cold.viewmodel.WatchWallet;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import static com.cobo.cold.ui.fragment.main.PsbtTxConfirmFragment.showExportPsbtDialog;
import static com.cobo.cold.viewmodel.WatchWallet.PSBT_MULTISIG_SIGN_ID;

public class PsbtSignedTxFragment extends SignedTxFragment {

    private boolean isMultisig;

    @Override
    protected void displaySignResult(TxEntity txEntity) {
        isMultisig = txEntity.getSignId().equals(PSBT_MULTISIG_SIGN_ID);
        if (isMultisig){
            boolean signed = isSigned(txEntity);
            mBinding.txDetail.scanHint.setText(signed ? getString(R.string.broadcast_multisig_tx_hint)
                    : getString(R.string.export_multisig_tx_hint));
            //show bc32 animated qr code
            mBinding.txDetail.dynamicQrcodeLayout.qrcode.setVisibility(View.VISIBLE);
            mBinding.txDetail.dynamicQrcodeLayout.qrcode.setEncodingScheme(DynamicQrCodeView.EncodingScheme.Bc32);
            mBinding.txDetail.dynamicQrcodeLayout.qrcode.setData(Hex.toHexString(Base64.decode(txEntity.getSignedHex())));
            mBinding.txDetail.dynamicQrcodeLayout.hint.setVisibility(View.GONE);

            mBinding.txDetail.qrcodeLayout.qrcode.setVisibility(View.GONE);
            mBinding.txDetail.broadcastGuide.setVisibility(View.GONE);
            mBinding.txDetail.export.setVisibility(View.GONE);

            mBinding.txDetail.info.setVisibility(View.INVISIBLE);
            mBinding.txDetail.exportToSdcardHint.setVisibility(View.VISIBLE);
            mBinding.txDetail.exportToSdcardHint.setText(R.string.generic_qrcode_hint);
            mBinding.txDetail.exportToSdcardHint.setOnClickListener(v -> showExportDialog());

        } else if (watchWallet == WatchWallet.BLUE || watchWallet == WatchWallet.GENERIC) {
            if (watchWallet == WatchWallet.BLUE) {
                mBinding.txDetail.info.setOnClickListener(v -> showBlueWalletInfo());
            }
            mBinding.txDetail.scanHint.setText(mActivity.getString(R.string.use_wallet_to_broadcast,
                    watchWallet.getWalletName(mActivity)));
            if (watchWallet.supportBc32QrCode()) {
                mBinding.txDetail.dynamicQrcodeLayout.qrcode
                        .setEncodingScheme(DynamicQrCodeView.EncodingScheme.Bc32);
                mBinding.txDetail.dynamicQrcodeLayout.qrcode
                        .setData(Hex.toHexString(Base64.decode(txEntity.getSignedHex())));
            }

            mBinding.txDetail.exportToSdcardHint.setVisibility(View.GONE);
            mBinding.txDetail.qrcodeLayout.qrcode.setVisibility(View.GONE);
            mBinding.txDetail.dynamicQrcodeLayout.qrcode.setVisibility(View.VISIBLE);
            mBinding.txDetail.broadcastGuide.setVisibility(View.GONE);
            if (!watchWallet.supportSdcard()) {
                mBinding.txDetail.export.setVisibility(View.GONE);
            }
            if (watchWallet == WatchWallet.GENERIC) {
                mBinding.txDetail.dynamicQrcodeLayout.hint.setVisibility(View.GONE);
                mBinding.txDetail.info.setVisibility(View.INVISIBLE);
                mBinding.txDetail.exportToSdcardHint.setVisibility(View.VISIBLE);
                mBinding.txDetail.exportToSdcardHint.setText(R.string.generic_qrcode_hint);
                mBinding.txDetail.exportToSdcardHint.setOnClickListener(v -> showExportDialog());
                mBinding.txDetail.export.setVisibility(View.GONE);

            }

        } else if(watchWallet == WatchWallet.WASABI || watchWallet == WatchWallet.BTCPAY) {
            mBinding.txDetail.qr.setVisibility(View.GONE);
            mBinding.txDetail.broadcastGuide.setGravity(Gravity.START);
            mBinding.txDetail.broadcastGuide.setText(getBroadcastGuideText());
        }
    }

    private int getBroadcastGuideText() {
        if (watchWallet == WatchWallet.WASABI) {
            return R.string.wasabi_broadcast_guide;
        } else if (watchWallet == WatchWallet.BTCPAY) {
            return R.string.btcpay_broadcast_guide;
        }
        return 0;
    }

    private boolean isSigned(TxEntity txEntity) {
        String signStatus = txEntity.getSignStatus();
        String[] splits = signStatus.split("-");
        int sigNumber = Integer.parseInt(splits[0]);
        int reqSigNumber = Integer.parseInt(splits[1]);
        return sigNumber >= reqSigNumber;
    }

    private void showBlueWalletInfo() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.blue_wallet_broadcast_guide);
        binding.subTitle.setText(R.string.blue_wallet_broadcast_guide1);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    @Override
    protected void showExportDialog() {
        Runnable runnable;
        if (isMultisig) {
            runnable = () -> popBackStack(R.id.assetFragment, false);
        } else {
            runnable = ()-> popBackStack(R.id.multisigFragment, false);
        }
        showExportPsbtDialog(mActivity, txEntity, runnable);
    }
}
