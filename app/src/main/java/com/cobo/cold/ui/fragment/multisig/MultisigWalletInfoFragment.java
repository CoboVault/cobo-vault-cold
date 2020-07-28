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

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.coinlib.ExtendPubkeyFormat;
import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.MultisigWalletInfoBinding;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.util.Keyboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MultisigWalletInfoFragment extends MultiSigBaseFragment<MultisigWalletInfoBinding> {

    private MultiSigWalletEntity wallet;
    private boolean isEditing;
    @Override
    protected int setView() {
        return R.layout.multisig_wallet_info;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            navigateUp();
            Keyboard.hide(mActivity, mBinding.walletName);
        });
        subscribeGetWallet(data);
    }

    private void subscribeGetWallet(Bundle data) {
        viewModel.getWalletEntity(data.getString("wallet_fingerprint"))
                .observe(this, w -> {
                    wallet = w;
                    setBindings(w);
                });
    }

    private void setBindings(MultiSigWalletEntity w) {
        mBinding.setWallet(w);
        mBinding.setAddressType(MultiSig.Account.ofPath(w.getExPubPath()).getFormat());
        mBinding.setXpubInfo(getXpub(w));
        mBinding.showAsXpub.setOnClickListener(v -> showAsXpub());
        mBinding.edit.setOnClickListener(v -> onEditClick());
    }

    private void onEditClick() {
        if (!isEditing) {
            isEditing = true;
            mBinding.walletName.setEnabled(true);
            mBinding.walletName.requestFocus();
            mBinding.edit.setAlpha(1f);
            mBinding.walletName.setSelection(mBinding.walletName.getText().length());
            Keyboard.show(mBinding.walletName.getContext(), mBinding.walletName);
        } else {
            isEditing = false;
            mBinding.walletName.clearFocus();
            mBinding.walletName.setEnabled(false);
            mBinding.edit.setAlpha(0.5f);
            viewModel.updateWallet(wallet);
            Keyboard.hide(mBinding.walletName.getContext(), mBinding.walletName);
        }
    }

    private void showAsXpub() {
        ModalDialog dialog = new ModalDialog();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.common_modal, null, false);
        binding.title.setText(R.string.check_xpub_info);
        binding.subTitle.setText(getDisplayXpubInfoForCC(wallet));
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.confirm.setVisibility(View.GONE);
        dialog.setBinding(binding);
        dialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private String getDisplayXpubInfoForCC(MultiSigWalletEntity wallet) {
        StringBuilder builder = new StringBuilder();
        try {
            JSONArray array = new JSONArray(wallet.getExPubs());
            for (int i = 0; i < wallet.getTotal(); i++) {
                JSONObject info = array.getJSONObject(i);
                builder.append(i + 1).append(". ").append(info.getString("xfp")).append("<br>")
                        .append(ExtendPubkeyFormat.convertExtendPubkey(info.getString("xpub"),
                                ExtendPubkeyFormat.xpub)).append("<br><br>");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    static String getXpub(MultiSigWalletEntity wallet) {
        StringBuilder builder = new StringBuilder();
        try {
            JSONArray array = new JSONArray(wallet.getExPubs());
            for (int i = 0; i < wallet.getTotal(); i++) {
                JSONObject info = array.getJSONObject(i);
                builder.append(i + 1).append(". ").append(info.getString("xfp")).append("\n")
                        .append(info.getString("xpub")).append("\n");
                if (i < wallet.getTotal() - 1) builder.append("\n");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
