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
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.ExportMultisigExpubBinding;
import com.cobo.cold.databinding.ModalWithTwoButtonBinding;
import com.cobo.cold.databinding.SwitchXpubBottomSheetBinding;
import com.cobo.cold.ui.modal.ExportToSdcardDialog;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.Storage;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Map;

import static com.cobo.cold.viewmodel.GlobalViewModel.exportSuccess;
import static com.cobo.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.cobo.cold.viewmodel.GlobalViewModel.writeToSdcard;

public class ExportMultisigExpubFragment extends MultiSigBaseFragment<ExportMultisigExpubBinding>
        implements Toolbar.OnMenuItemClickListener {
    public static final String TAG = "ExportMultisigExpubFragment";
    private MultiSig.Account account = MultiSig.Account.P2WSH;
    @Override
    protected int setView() {
        return R.layout.export_multisig_expub;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbar.inflateMenu(R.menu.export_all);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        updateUI();
        mBinding.addressType.setOnClickListener(v -> showBottomSheetMenu());
        mBinding.exportToSdcard.setOnClickListener(v -> exportToSdcard());
    }

    private void exportToSdcard() {
        Storage storage = Storage.createByEnvironment(mActivity);
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            String fileName = viewModel.getExportXpubFileName(account);
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.modal_with_two_button,
                    null, false);
            binding.title.setText(R.string.export_multisig_xpub);
            binding.subTitle.setText(R.string.file_name_label);
            binding.actionHint.setText(fileName);
            binding.actionHint.setTypeface(Typeface.DEFAULT_BOLD);
            binding.left.setText(R.string.cancel);
            binding.left.setOnClickListener(v -> dialog.dismiss());
            binding.right.setText(R.string.export);
            binding.right.setOnClickListener(v -> {
                dialog.dismiss();
                if (writeToSdcard(storage, viewModel.getExportXpubInfo(account), fileName)) {
                    exportSuccess(mActivity, null);
                }
            });
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }

    private void exportAllToSdcard() {
        Storage storage = Storage.createByEnvironment(mActivity);
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            String fileName = viewModel.getExportAllXpubFileName();
            if (writeToSdcard(storage, viewModel.getExportAllXpubInfo(), fileName)) {
                ExportToSdcardDialog dialog = ExportToSdcardDialog.newInstance(fileName);
                dialog.show(mActivity.getSupportFragmentManager(), "");
                new Handler().postDelayed(dialog::dismiss, 1000);
            }
        }
    }


    private void updateUI() {
        String accountType = getAccountTypeString(account);
        String xpub = viewModel.getXpub(account);
        mBinding.addressType.setText(String.format("%s ", accountType));
        mBinding.expub.setText(xpub);
        mBinding.path.setText(String.format("(%s)", account.getPath()));
        mBinding.qrcode.setData(viewModel.getExportXpubInfo(account));
    }

    private String getAccountTypeString(MultiSig.Account account) {
        int accountType = R.string.multi_sig_account_segwit;
        switch (account) {
            case P2WSH_P2SH:
                accountType = R.string.multi_sig_account_p2sh;
                break;
            case P2SH:
                accountType = R.string.multi_sig_account_legacy;
                break;
            case P2WSH:
                break;
        }
        return getString(accountType);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export_all) {
            ModalDialog dialog = new ModalDialog();
            CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.common_modal, null, false);
            binding.title.setText(getString(R.string.extend_pubkey));
            binding.subTitle.setText(getAllExtendPubkeyInfo());
            binding.subTitle.setGravity(Gravity.START);
            binding.close.setOnClickListener(v -> dialog.dismiss());
            binding.confirm.setText(getString(R.string.export));
            binding.confirm.setOnClickListener(v -> {
                exportAllToSdcard();
                dialog.dismiss();
            });
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");

        }
        return true;
    }

    private String getAllExtendPubkeyInfo() {
        StringBuilder info = new StringBuilder("<br>");
        for (Map.Entry<MultiSig.Account, String> entry : viewModel.getAllXpubs().entrySet()) {
            info.append(String.format("%s(%s)",getAccountTypeString(entry.getKey()),entry.getKey().getFormat())).append("<br>")
                    .append(entry.getKey().getPath()).append("<br>")
                    .append(entry.getValue()).append("<br><br>");
        }

        return info.toString();
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        SwitchXpubBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.switch_xpub_bottom_sheet, null, false);
        refreshCheckedStatus(binding.getRoot());
        binding.nativeSegwit.setOnClickListener(v -> onXpubSwitch(dialog, MultiSig.Account.P2WSH));
        binding.nestedSegeit.setOnClickListener(v -> onXpubSwitch(dialog, MultiSig.Account.P2WSH_P2SH));
        binding.legacy.setOnClickListener(v -> onXpubSwitch(dialog, MultiSig.Account.P2SH));
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void onXpubSwitch(BottomSheetDialog dialog,
                              MultiSig.Account account) {
        this.account = account;
        dialog.dismiss();
        updateUI();
    }

    private void refreshCheckedStatus(View view) {
        for (MultiSig.Account value : MultiSig.Account.values()) {
            view.findViewWithTag(value.getFormat()).setVisibility(View.GONE);
        }
        view.findViewWithTag(account.getFormat()).setVisibility(View.VISIBLE);
    }
}
