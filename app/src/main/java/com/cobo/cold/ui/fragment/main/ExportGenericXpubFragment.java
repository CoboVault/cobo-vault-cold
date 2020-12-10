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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.coinlib.ExtendPubkeyFormat;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.ExportSdcardModalBinding;
import com.cobo.cold.databinding.ExportXpubGenericBinding;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.Storage;
import com.cobo.cold.viewmodel.GlobalViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cobo.coinlib.ExtendPubkeyFormat.convertExtendPubkey;
import static com.cobo.cold.viewmodel.GlobalViewModel.exportSuccess;
import static com.cobo.cold.viewmodel.GlobalViewModel.getAccount;
import static com.cobo.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.cobo.cold.viewmodel.GlobalViewModel.writeToSdcard;


public class ExportGenericXpubFragment extends BaseFragment<ExportXpubGenericBinding> {

    private JSONObject xpubInfo;
    private WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.export_xpub_generic;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        try {
            xpubInfo = GlobalViewModel.getXpubInfo(mActivity);
            String exPub = xpubInfo.getString("ExtPubKey");
            exPub = convertExtpub(exPub, getAccount(mActivity));
            xpubInfo.put("ExtPubKey", exPub);
            mBinding.qrcode.setData(xpubInfo.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBinding.done.setOnClickListener(v -> handleButtonClick());
        mBinding.skip.setOnClickListener(v -> popBackStack(R.id.assetFragment,false));
        mBinding.exportToSdcard.setOnClickListener(v -> exportToSdcard());
        refreshScanHint();
    }

    private void refreshScanHint() {
        if (watchWallet == WatchWallet.BTCPAY) {
            mBinding.skip.setVisibility(View.GONE);
            mBinding.scanHint.setText(R.string.scan_xpub_qrcode_with_btc_pay);
            mBinding.info.setVisibility(View.VISIBLE);
            mBinding.info.setOnClickListener(v ->
                    showExportGuide(getString(R.string.export_xpub_guide_text1_btcpay), getString(R.string.export_xpub_guide_text2_btcpay)));
        }
    }
    private void showExportGuide(String title, String content) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(title);
        binding.subTitle.setText(content);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void handleButtonClick() {
        if (mActivity instanceof SetupVaultActivity) {
            navigate(R.id.action_to_setupCompleteFragment);
        } else {
            popBackStack(R.id.assetFragment, false);
        }
    }

    private void exportToSdcard() {
        Storage storage = Storage.createByEnvironment(mActivity);
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            ModalDialog modalDialog = ModalDialog.newInstance();
            ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.export_sdcard_modal, null, false);
            binding.title.setText(R.string.export_xpub_text_file);
            binding.fileName.setText(getFileName());
            binding.actionHint.setText(R.string.electrum_import_xpub_action);
            binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
            binding.confirm.setOnClickListener(vv -> {
                modalDialog.dismiss();
                if (writeToSdcard(storage, xpubInfo.toString(), getFileName())) {
                    exportSuccess(mActivity, null);
                }
            });
            modalDialog.setBinding(binding);
            modalDialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }

    private String getFileName() {
        Coins.Account account = GlobalViewModel.getAccount(mActivity);
        switch (account) {
            case SegWit:
            case SegWit_TESTNET:
                return "p2wpkh-pubkey.txt";
            case P2SH:
            case P2SH_TESTNET:
                return "p2wpkh-p2sh-pubkey.txt";
            case P2PKH:
            case P2PKH_TESTNET:
                return "p2pkh-pubkey.txt";
        }
        return "p2wpkh-p2sh-pubkey.txt";
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String convertExtpub(String expub, Coins.Account account) {
        return convertExtendPubkey(expub, ExtendPubkeyFormat.valueOf(account.getXpubPrefix()));
    }
}
