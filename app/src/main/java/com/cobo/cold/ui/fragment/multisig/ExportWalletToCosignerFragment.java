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

package com.cobo.cold.ui.fragment.multisig;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.cold.R;
import com.cobo.cold.databinding.ExportWalletToCosignerBinding;
import com.cobo.cold.databinding.ModalWithTwoButtonBinding;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.ui.views.qrcode.DynamicQrCodeView;
import com.cobo.cold.update.utils.Storage;

import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.cobo.cold.viewmodel.GlobalViewModel.exportSuccess;
import static com.cobo.cold.viewmodel.GlobalViewModel.hasSdcard;
import static com.cobo.cold.viewmodel.GlobalViewModel.writeToSdcard;

public class ExportWalletToCosignerFragment extends MultiSigBaseFragment<ExportWalletToCosignerBinding> {
    private MultiSigWalletEntity walletEntity;
    private Storage storage;
    private String walletFileContent;

    @Override
    protected int setView() {
        return R.layout.export_wallet_to_cosigner;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        storage = Storage.createByEnvironment(mActivity);
        String walletFingerprint = data.getString("wallet_fingerprint");
        boolean isSetup = data.getBoolean("setup");
        if (!isSetup) {
            mBinding.skip.setVisibility(View.GONE);
            mBinding.exportToElectrum.setVisibility(View.GONE);
        } else {
            mBinding.skip.setVisibility(View.VISIBLE);
            mBinding.exportToElectrum.setVisibility(View.VISIBLE);
            Bundle bundle = getArguments();
            bundle.putBoolean("isImportMultisig", true);
            //View.OnClickListener onClickListener = v -> navigate(R.id.action_export_wallet_to_electrum, bundle);
            View.OnClickListener onClickListener = v -> popBackStack(R.id.multisigFragment, false);
            mBinding.skip.setOnClickListener(onClickListener);
            mBinding.exportToElectrum.setOnClickListener(onClickListener);
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.qrcodeLayout.hint.setVisibility(View.GONE);
        viewModel.exportWalletToCosigner(walletFingerprint).observe(this, s -> {
            walletFileContent = s;
            mBinding.qrcodeLayout.qrcode.setEncodingScheme(DynamicQrCodeView.EncodingScheme.Bc32);
            mBinding.qrcodeLayout.qrcode.setData(Hex.toHexString(s.getBytes(StandardCharsets.UTF_8)));
        });

        viewModel.getWalletEntity(walletFingerprint).observe(this,
                w -> {
                    walletEntity = w;
                    mBinding.verifyCode.setText(getString(R.string.wallet_verification_code, w.getVerifyCode()));
                });
        mBinding.exportToSdcard.setOnClickListener(v -> handleExportWallet());
        mBinding.info.setOnClickListener(v -> ModalDialog.showCommonModal(mActivity,
                getString(R.string.wallet_verify_code),
                getString(R.string.check_verify_code_hint),
                getString(R.string.know),
                null));

    }

    private void handleExportWallet() {
        if (hasSdcard(mActivity)) {
            String fileName = String.format("export_%s.txt", walletEntity.getWalletName());
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.modal_with_two_button,
                    null, false);
            binding.title.setText(R.string.export_multisig_to_cosigner);
            binding.subTitle.setText(R.string.file_name_label);
            binding.actionHint.setText(fileName);
            binding.actionHint.setTypeface(Typeface.DEFAULT_BOLD);
            binding.left.setText(R.string.cancel);
            binding.left.setOnClickListener(left -> dialog.dismiss());
            binding.right.setText(R.string.export);
            binding.right.setOnClickListener(right -> {
                dialog.dismiss();
                if (writeToSdcard(storage, walletFileContent, fileName)) {
                    exportSuccess(mActivity, null);
                }
            });
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");
        } else {
            ModalDialog.showCommonModal(mActivity, getString(R.string.no_sdcard),
                    getString(R.string.no_sdcard_hint),getString(R.string.know),null);
        }
    }
}
