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
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.exception.CoinNotFindException;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.utils.Base43;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.QrcodeScanFragmentBinding;
import com.cobo.cold.scan.CaptureHandler;
import com.cobo.cold.scan.Host;
import com.cobo.cold.scan.ScannedData;
import com.cobo.cold.scan.bean.ZxingConfig;
import com.cobo.cold.scan.bean.ZxingConfigBuilder;
import com.cobo.cold.scan.camera.CameraManager;
import com.cobo.cold.scan.view.PreviewFrame;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.viewmodel.GlobalViewModel;
import com.cobo.cold.viewmodel.InvalidMultisigWalletException;
import com.cobo.cold.viewmodel.MultiSigViewModel;
import com.cobo.cold.viewmodel.QrScanViewModel;
import com.cobo.cold.viewmodel.SharedDataViewModel;
import com.cobo.cold.viewmodel.UnknowQrCodeException;
import com.cobo.cold.viewmodel.UuidNotMatchException;
import com.cobo.cold.viewmodel.WatchWallet;
import com.cobo.cold.viewmodel.WatchWalletNotMatchException;
import com.cobo.cold.viewmodel.XfpNotMatchException;
import com.cobo.cold.viewmodel.XpubNotMatchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.EncoderException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;
import static com.cobo.cold.viewmodel.MultiSigViewModel.decodeColdCardWalletFile;
import static com.cobo.cold.viewmodel.WatchWallet.ELECTRUM;
import static com.cobo.cold.viewmodel.WatchWallet.getWatchWallet;

public class QRCodeScanFragment extends BaseFragment<QrcodeScanFragmentBinding>
        implements SurfaceHolder.Callback, Host {

    private CameraManager mCameraManager;
    private CaptureHandler mHandler;
    private boolean hasSurface;
    private ZxingConfig mConfig;
    private SurfaceHolder mSurfaceHolder;

    private QrScanPurpose qrScanPurpose;

    private QrScanViewModel viewModel;
    private ModalDialog dialog;
    private WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.qrcode_scan_fragment;
    }

    @Override
    protected void init(View view) {
        watchWallet = getWatchWallet(mActivity);
        mBinding.scanHint.setText(getScanhint());
        boolean isSetupVault = getArguments() != null && getArguments().getBoolean(IS_SETUP_VAULT);
        String purpose = getArguments() != null ? getArguments().getString("purpose") : "";
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mConfig = new ZxingConfigBuilder()
                .setIsFullScreenScan(true)
                .setFrameColor(R.color.colorAccent)
                .createZxingConfig();
        mCameraManager = new CameraManager(mActivity, mConfig);
        mBinding.frameView.setCameraManager(mCameraManager);
        mBinding.frameView.setZxingConfig(mConfig);
        QrScanViewModel.Factory factory = new QrScanViewModel.Factory(mActivity.getApplication(), isSetupVault);
        viewModel = ViewModelProviders.of(this, factory).get(QrScanViewModel.class);
        if (!TextUtils.isEmpty(purpose)) {
            mBinding.scanHint.setVisibility(View.GONE);
        }
        qrScanPurpose = QrScanPurpose.ofPurpose(purpose);
    }

    private String getScanhint() {
        switch (watchWallet){
            case ELECTRUM:
                return getString(R.string.scan_electrum_hint);
            case BLUE:
                return getString(R.string.scan_blue_hint);
            case WASABI:
                return getString(R.string.scan_wasabi_hint);
            case BTCPAY:
                return getString(R.string.btcpay_scan_hint);

        }
        return "";
    }
    @Override
    public void onResume() {
        super.onResume();
        mSurfaceHolder = mBinding.preview.getHolder();
        if (hasSurface) {
            initCamera(mSurfaceHolder);
        } else {
            mSurfaceHolder.addCallback(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.quitSynchronously();
            mHandler = null;
        }
        mCameraManager.closeDriver();

        if (!hasSurface) {
            mSurfaceHolder.removeCallback(this);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(surfaceHolder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        hasSurface = false;
    }

    private void initCamera(@NonNull SurfaceHolder surfaceHolder) {
        if (mCameraManager.isOpen()) {
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            if (mHandler == null) {
                mHandler = new CaptureHandler(this, mCameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    @Override
    public ZxingConfig getConfig() {
        return mConfig;
    }

    @Override
    public PreviewFrame getFrameView() {
        return mBinding.frameView;
    }

    @Override
    public void handleDecode(String res) {
        SharedDataViewModel viewModel =
                ViewModelProviders.of(mActivity).get(SharedDataViewModel.class);
        viewModel.updateScanResult(res);

        if (QrScanPurpose.WEB_AUTH == qrScanPurpose) {
            alert(getString(R.string.invalid_webauth_qrcode_hint));
        } else if (QrScanPurpose.ADDRESS == qrScanPurpose) {
            navigateUp();
        } else if(QrScanPurpose.COLLECT_XPUB == qrScanPurpose){
            navigateUp();
        } else if(QrScanPurpose.IMPORT_MULTISIG_WALLET == qrScanPurpose){
            alert(getString(R.string.unsupported_qrcode));
        } else if(isElectrumPsbtTx(res)){
            String psbtBase64 = Base64.toBase64String(Base43.decode(res));
            Bundle bundle = new Bundle();
            bundle.putString("psbt_base64", psbtBase64);
            bundle.putBoolean("multisig", qrScanPurpose == QrScanPurpose.MULTISIG_TX);
            navigate(R.id.action_to_psbtTxConfirmFragment, bundle);
        } else {
            alert(getString(R.string.unsupported_qrcode));
        }
    }

    public QrScanPurpose getPurpose() {
        return qrScanPurpose;
    }

    public void handleImportMultisigWallet(String hex) {
        try {
            MultiSigViewModel viewModel = ViewModelProviders.of(mActivity).get(MultiSigViewModel.class);
            String xfp = viewModel.getXfp();
            JSONObject obj = decodeColdCardWalletFile(new String(Hex.decode(hex), StandardCharsets.UTF_8));
            Bundle bundle = new Bundle();
            bundle.putString("wallet_info",obj.toString());
            JSONArray array = obj.getJSONArray("Xpubs");
            boolean matchXfp = false;
            for (int i = 0 ; i < array.length(); i++) {
                JSONObject xpubInfo = array.getJSONObject(i);
                if (xpubInfo.getString("xfp").equalsIgnoreCase(xfp)) {
                    matchXfp = true;
                    break;
                }
            }
            if (!matchXfp) {
               throw new XfpNotMatchException("xfp not match");
            } else {
                navigate(R.id.import_multisig_wallet, bundle);
            }
        } catch (XfpNotMatchException e) {
            e.printStackTrace();
            alert(getString(R.string.import_multisig_wallet_fail),getString(R.string.import_multisig_wallet_fail_hint));
        } catch (InvalidMultisigWalletException e) {
            e.printStackTrace();
            alert(getString(R.string.invalid_multisig_wallet),getString(R.string.invalid_multisig_wallet_hint));
        } catch (JSONException e) {
            e.printStackTrace();
            alert(getString(R.string.incorrect_qrcode));
        }

    }

    private boolean isElectrumPsbtTx(String res) {
        try {
            byte[] data = Base43.decode(res);
            return new String(data).startsWith("psbt");
        } catch (EncoderException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void handleDecode(ScannedData[] res) {
        try {
            if (!qrScanPurpose.isAnimateQr()) {
                alert(getString(R.string.unsupported_qrcode));
            } else {
                viewModel.handleDecode(this, res);
            }
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            alert(getString(R.string.incorrect_tx_data));
        } catch (JSONException e) {
            e.printStackTrace();
            alert(getString(R.string.incorrect_qrcode));
        } catch (CoinNotFindException e) {
            e.printStackTrace();
            alert(null, getString(R.string.only_support_btc), null);
        } catch (UuidNotMatchException e) {
            e.printStackTrace();
            alert(getString(R.string.uuid_not_match));
        } catch (UnknowQrCodeException e) {
            e.printStackTrace();
            alert(getString(R.string.unsupported_qrcode));
        } catch (WatchWalletNotMatchException e) {
            e.printStackTrace();
            alert(getString(R.string.identification_failed),
                    getString(R.string.master_pubkey_not_match)
                            + getString(R.string.watch_wallet_not_match,
                            WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
        } catch (InvalidMultisigWalletException e) {
            e.printStackTrace();
            alert(getString(R.string.identification_failed),
                    getString(R.string.master_pubkey_not_match)
                            + getString(R.string.watch_wallet_not_match,
                            WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
        }
    }

    @Override
    public void handleProgress(int total, int scan) {
        mBinding.setProgress(getString(R.string.scan_progress, scan + "/" + total));
    }

    @Override
    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }

    private void alert(String message) {
        alert(null, message);
    }

    private void alert(String title, String message) {
        alert(title, message, null);
    }

    private void alert(String title, String message, Runnable run) {
        dialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        if (title != null) {
            binding.title.setText(title);
        } else {
            binding.title.setText(R.string.scan_failed);
        }
        binding.subTitle.setText(message);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (run != null) {
                run.run();
            } else {
                mBinding.setProgress("");
                if (mHandler != null) {
                    mHandler.restartPreviewAndDecode();
                }
            }
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "scan fail");
    }

}


