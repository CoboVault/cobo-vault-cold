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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.ExportSuccessBinding;
import com.cobo.cold.databinding.ImportWalletBinding;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.viewmodel.XfpNotMatchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cobo.cold.ui.fragment.multisig.MultisigWalletInfoFragment.getXpub;

public class ImportWalletFragment extends MultiSigBaseFragment<ImportWalletBinding> {

    private JSONObject walletInfo;
    private MultiSig.Account account;
    private int threshold;
    private String creator;
    private MultiSigWalletEntity dummyWallet;

    @Override
    protected int setView() {
        return R.layout.import_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        dummyWallet = constructWalletEntity(data);
        mBinding.setWallet(dummyWallet);
        mBinding.setAddressType(MultiSig.Account.ofPath(dummyWallet.getExPubPath()).getFormat());
        mBinding.setXpubInfo(getXpub(dummyWallet));
        mBinding.confirm.setOnClickListener(v -> showVerifyCode());
        mBinding.cancel.setOnClickListener(v -> navigateUp());
        showCheckDialog();

    }

    private void showCheckDialog() {
        ModalDialog.showCommonModal(mActivity,getString(R.string.please_check_multisig_wallet_info),
                getString(R.string.check_multisig_wallet_hint),
                getString(R.string.know), null);
    }

    private void showVerifyCode() {
        if ("CoboVault".equals(creator)) {
            try {
                List<String> xpubs = new ArrayList<>();
                JSONArray array = new JSONArray(dummyWallet.getExPubs());
                for (int i = 0; i < array.length(); i++) {
                    xpubs.add(array.getJSONObject(i).getString("xpub"));
                }
                ModalDialog dialog = new ModalDialog();
                CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.common_modal, null, false);
                binding.title.setText(R.string.verify_multisig_wallet);
                binding.subTitle.setText(getString(R.string.verify_wallet_hint,
                        viewModel.calculateWalletVerifyCode(threshold, xpubs, account.getPath())));
                binding.close.setVisibility(View.GONE);
                binding.confirm.setText(R.string.verify_code_ok);
                binding.confirm.setOnClickListener(v -> {
                    importWallet();
                    dialog.dismiss();
                });
                binding.btn1.setVisibility(View.VISIBLE);
                binding.btn1.setText(R.string.error_verify_code);
                binding.btn1.setOnClickListener(v -> dialog.dismiss());
                dialog.setBinding(binding);
                dialog.show(mActivity.getSupportFragmentManager(), "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            importWallet();
        }
    }

    private MultiSigWalletEntity constructWalletEntity(Bundle data) {
        try {
            walletInfo = new JSONObject(data.getString("wallet_info"));
            threshold = Integer.parseInt(walletInfo.getString("Policy").split(" of ")[0]);
            int total = Integer.parseInt(walletInfo.getString("Policy").split(" of ")[1]);
            account = MultiSig.Account.ofPath(walletInfo.getString("Derivation"));
            creator = walletInfo.optString("Creator");

            return new MultiSigWalletEntity(walletInfo.getString("Name"),
                    threshold, total,account.getPath(),walletInfo.getJSONArray("Xpubs").toString(),"","","");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void importWallet() {
        try {
            viewModel.createMultisigWallet(threshold, account, walletInfo.getJSONArray("Xpubs"))
                    .observe(this, this::onImportWalletSuccess);
        } catch (XfpNotMatchException e) {
            e.printStackTrace();
            ModalDialog.showCommonModal(mActivity,getString(R.string.import_failed),
                    getString(R.string.not_include_current_vault)
                    ,getString(R.string.know),null);
        } catch (JSONException e) {
            e.printStackTrace();
            ModalDialog.showCommonModal(mActivity,getString(R.string.not_valid_multisig_wallet),
                    getString(R.string.invalid_wallet_hint)
                    ,getString(R.string.know),null);
        }

    }

    private void onImportWalletSuccess(MultiSigWalletEntity walletEntity) {
        Handler handler = new Handler();
        if (walletEntity != null) {
            ModalDialog dialog = ModalDialog.newInstance();
            ExportSuccessBinding binding =
                    DataBindingUtil.inflate(LayoutInflater.from(mActivity),R.layout.export_success,
                            null,false);
            binding.text.setText(R.string.import_success);
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(),"");
            handler.postDelayed(() -> {
                dialog.dismiss();
                popBackStack(R.id.multisigFragment,false);
                //Bundle bundle = Bundle.forPair("wallet_fingerprint", walletEntity.getWalletFingerPrint());
                //bundle.putBoolean("isImportMultisig",true);
                //navigate(R.id.action_export_wallet_to_electrum, bundle);
            },500);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
