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

    }

    private void handleExportWallet() {
        String fileName = String.format("export-CoboVault-%dof%d.txt", walletEntity.getThreshold(), walletEntity.getTotal());
        ModalDialog dialog = new ModalDialog();
        ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.modal_with_two_button,
                null, false);
        binding.title.setText("导出钱包给参与方");
        binding.subTitle.setText("文件名称:");
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
    }
}
