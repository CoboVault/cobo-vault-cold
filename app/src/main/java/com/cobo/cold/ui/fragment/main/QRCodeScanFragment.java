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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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

import com.cobo.coinlib.coins.BTC.Electrum.ElectrumTx;
import com.cobo.coinlib.coins.DOT.UOSDecoder;
import com.cobo.coinlib.exception.CoinNotFindException;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.exception.InvalidUOSException;
import com.cobo.coinlib.utils.Base43;
import com.cobo.cold.R;
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
import com.cobo.cold.viewmodel.ElectrumViewModel;
import com.cobo.cold.viewmodel.QrScanViewModel;
import com.cobo.cold.viewmodel.SharedDataViewModel;
import com.cobo.cold.viewmodel.UnknowQrCodeException;
import com.cobo.cold.viewmodel.UuidNotMatchException;
import com.cobo.cold.viewmodel.WatchWallet;
import com.cobo.cold.viewmodel.XpubNotMatchException;

import org.json.JSONException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;
import static com.cobo.cold.ui.fragment.main.TxConfirmFragment.KEY_TX_DATA;

public class QRCodeScanFragment extends BaseFragment<QrcodeScanFragmentBinding>
        implements SurfaceHolder.Callback, Host {

    private CameraManager mCameraManager;
    private CaptureHandler mHandler;
    private boolean hasSurface;
    private ZxingConfig mConfig;
    private SurfaceHolder mSurfaceHolder;

    private String purpose;

    private QrScanViewModel viewModel;
    private ModalDialog dialog;

    private ObjectAnimator scanLineAnimator;

    @Override
    protected int setView() {
        return R.layout.qrcode_scan_fragment;
    }

    @Override
    protected void init(View view) {
        boolean isSetupVault = getArguments() != null && getArguments().getBoolean(IS_SETUP_VAULT);
        purpose = getArguments() != null ? getArguments().getString("purpose") : "";
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
            mBinding.electrumScanHint.setVisibility(View.GONE);
        }

        scanLineAnimator = ObjectAnimator.ofFloat(mBinding.scanLine, "translationY",0, 600);
        scanLineAnimator.setDuration(2000L);
        scanLineAnimator.setRepeatCount(ValueAnimator.INFINITE);
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
        scanLineAnimator.cancel();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        scanLineAnimator.start();
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
        if ("webAuth".equals(purpose)) {
            alert(getString(R.string.invalid_webauth_qrcode_hint));
        } else if ("address".equals(purpose)) {
            navigateUp();
        } else {
            alert(getString(R.string.unresolve_tx),
                    getString(R.string.unresolve_tx_hint,
                            WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
//            try {
//                if (tryParseElecturmTx(res) != null) {
//                    handleElectrumTx(res);
//                } else if(tryDecodePolkadotjsTx(res) != null) {
//                    handlePolkadotJsTx(res);
//                } else {
//                    alert(getString(R.string.unresolve_tx),
//                            getString(R.string.unresolve_tx_hint,
//                                    WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
//                }
//            } catch (XpubNotMatchException e) {
//                alert(getString(R.string.identification_failed),
//                        getString(R.string.master_pubkey_not_match));
//            }
        }
    }

    private void handleElectrumTx(String res) {
        String data = Hex.toHexString(Base43.decode(res));
        Bundle bundle = new Bundle();
        bundle.putString("txn", data);
        navigate(R.id.action_to_ElectrumTxConfirmFragment, bundle);
    }

    private void handlePolkadotJsTx(String res) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TX_DATA, res);
        bundle.putBoolean("substrateTx", true);
        navigate(R.id.action_to_txConfirmFragment, bundle);
    }

    private ElectrumTx tryParseElecturmTx(String res) throws XpubNotMatchException {
        try {
            byte[] data = Base43.decode(res);
            ElectrumTx tx = ElectrumTx.parse(data);
            if (!checkElectrumExpub(tx)) {
                throw new XpubNotMatchException("xpub not match");
            }
            return tx;
        } catch (ElectrumTx.SerializationException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    private UOSDecoder.UOSDecodeResult tryDecodePolkadotjsTx(String res) {
        try {
            UOSDecoder decoder = new UOSDecoder();
            return decoder.decodeUOSRawData(res,false);
        } catch (InvalidUOSException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean checkElectrumExpub(ElectrumTx tx) {
        String xpub = ViewModelProviders.of(mActivity).get(ElectrumViewModel.class).getXpub();
        return tx.getInputs()
                .stream()
                .allMatch(input -> xpub.equals(input.pubKey.xpub));
    }

    @Override
    public void handleDecode(ScannedData[] res) {
        try {
            viewModel.handleDecode(this, res);
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            alert(getString(R.string.unresolve_tx),
                    getString(R.string.unresolve_tx_hint,
                            WatchWallet.getWatchWallet(mActivity).getWalletName(mActivity)));
        } catch (JSONException e) {
            e.printStackTrace();
            alert(getString(R.string.incorrect_qrcode));
        } catch (CoinNotFindException e) {
            e.printStackTrace();
            alert(null,getString(R.string.version_not_match), null);
        } catch (UuidNotMatchException e) {
            e.printStackTrace();
            alert(getString(R.string.uuid_not_match));
        } catch (UnknowQrCodeException e) {
            e.printStackTrace();
            alert(getString(R.string.unsupported_qrcode));
        }
    }

    @Override
    public void handleProgress(int total, int scan) {
        mBinding.scanProgress.setText(getString(R.string.scan_progress, scan + "/" + total));
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
                mBinding.scanProgress.setText("");
                if (mHandler != null) {
                    mHandler.restartPreviewAndDecode();
                }
            }
        });
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "scan fail");
    }

}


