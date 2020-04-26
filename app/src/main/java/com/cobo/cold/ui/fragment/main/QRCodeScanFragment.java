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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.exception.CoinNotFindException;
import com.cobo.coinlib.exception.InvalidTransactionException;
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
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.viewmodel.QrScanViewModel;
import com.cobo.cold.viewmodel.SharedDataViewModel;
import com.cobo.cold.viewmodel.UnknowQrCodeException;
import com.cobo.cold.viewmodel.UuidNotMatchException;

import org.json.JSONException;

import java.io.IOException;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;

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
        if ("webAuth".equals(purpose)) {
            alert(getString(R.string.invalid_webauth_qrcode_hint));
        } else if ("address".equals(purpose)) {
            navigateUp();
        } else {
            alert(getString(R.string.unsupported_qrcode));
        }
    }

    @Override
    public void handleDecode(ScannedData[] res) {
        try {
            viewModel.handleDecode(this, res);
        } catch (InvalidTransactionException e) {
            e.printStackTrace();
            alert(getString(R.string.incorrect_tx_data));
        } catch (JSONException e) {
            e.printStackTrace();
            alert(getString(R.string.incorrect_qrcode));
        } catch (CoinNotFindException e) {
            e.printStackTrace();
            alert(getString(R.string.version_not_match), () -> {
                navigateUp();
                ((MainActivity) mActivity).getNavController().navigateUp();
                ((MainActivity) mActivity).getNavController().navigate(R.id.action_to_settingFragment);
            });
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
        alert(message, null);
    }

    private void alert(String message, Runnable run) {
        dialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        binding.title.setText(R.string.scan_failed);
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


