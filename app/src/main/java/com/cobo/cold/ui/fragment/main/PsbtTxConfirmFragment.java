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
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cobo.cold.R;
import com.cobo.cold.databinding.ExportSdcardModalBinding;
import com.cobo.cold.ui.fragment.main.electrum.UnsignedTxFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.ui.views.AuthenticateModal;
import com.cobo.cold.update.utils.FileUtils;
import com.cobo.cold.update.utils.Storage;
import com.cobo.cold.viewmodel.WatchWallet;

import java.io.File;
import java.util.Objects;

import static com.cobo.cold.ui.fragment.main.BlueWalletBroadcastTxFragment.KEY_TXID;
import static com.cobo.cold.viewmodel.GlobalViewModel.exportSuccess;
import static com.cobo.cold.viewmodel.GlobalViewModel.hasSdcard;
import static com.cobo.cold.viewmodel.GlobalViewModel.showNoSdcardModal;

public class PsbtTxConfirmFragment extends UnsignedTxFragment {

    private String psbtBase64;
    @Override
    protected void init(View view) {
        super.init(view);
    }

    static void showExportPsbtDialog(AppCompatActivity activity, String txId, String psbt,
                                     Runnable onExportSuccess) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.export_sdcard_modal, null, false);
        String fileName = "signed_" + txId.substring(0, 8) + ".psbt";
        binding.title.setText(R.string.export_signed_txn);
        binding.fileName.setText(fileName);
        binding.actionHint.setVisibility(View.GONE);
        binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
        binding.confirm.setOnClickListener(vv -> {
            modalDialog.dismiss();
            if (hasSdcard(activity)) {
                Storage storage = Storage.createByEnvironment(activity);
                File file = new File(Objects.requireNonNull(storage).getExternalDir(), fileName);
                boolean result = FileUtils.writeString(file, psbt);
                if (result) {
                    exportSuccess(activity, onExportSuccess);
                }
            } else {
                showNoSdcardModal(activity);
            }
        });
        modalDialog.setBinding(binding);
        modalDialog.show(activity.getSupportFragmentManager(), "");
    }

    @Override
    protected AuthenticateModal.OnVerify signWithVerifyInfo() {
        return token -> {
            viewModel.setToken(token);
            viewModel.handleSignPsbt(psbtBase64);
            subscribeSignState();
        };
    }

    @Override
    protected void parseTx() {
        psbtBase64 = Objects.requireNonNull(getArguments()).getString("psbt_base64");
        viewModel.parsePsbtBase64(psbtBase64);
    }

    protected void onSignSuccess() {
        if (WatchWallet.getWatchWallet(mActivity) == WatchWallet.BLUE) {
            Bundle data = new Bundle();
            data.putString(KEY_TXID,viewModel.getTxId());
            navigate(R.id.action_to_blue_wallet_broadcast, data);
        } else {
            showExportPsbtDialog(mActivity, viewModel.getTxId(),
                    viewModel.getTxHex(), this::navigateUp);
        }
        viewModel.getSignState().removeObservers(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}



