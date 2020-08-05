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

package com.cobo.cold.ui.fragment.main.electrum;

import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.utils.Base43;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.callables.FingerprintPolicyCallable;
import com.cobo.cold.config.FeatureFlags;
import com.cobo.cold.databinding.ElectrumTxConfirmFragmentBinding;
import com.cobo.cold.databinding.ProgressModalBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.encryptioncore.utils.ByteFormatter;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.fragment.main.FeeAttackChecking;
import com.cobo.cold.ui.fragment.main.TransactionItem;
import com.cobo.cold.ui.fragment.main.TransactionItemAdapter;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.ui.modal.ProgressModalDialog;
import com.cobo.cold.ui.modal.SigningDialog;
import com.cobo.cold.ui.views.AuthenticateModal;
import com.cobo.cold.util.KeyStoreUtil;
import com.cobo.cold.viewmodel.GlobalViewModel;
import com.cobo.cold.viewmodel.NoMatchedMultisigWallet;
import com.cobo.cold.viewmodel.TxConfirmViewModel;
import com.cobo.cold.viewmodel.WatchWallet;
import com.cobo.cold.viewmodel.WatchWalletNotMatchException;
import com.cobo.cold.viewmodel.XpubNotMatchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cobo.cold.callables.FingerprintPolicyCallable.READ;
import static com.cobo.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.cobo.cold.ui.fragment.Constants.KEY_NAV_ID;
import static com.cobo.cold.ui.fragment.main.BroadcastTxFragment.KEY_TXID;
import static com.cobo.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.NORMAL;
import static com.cobo.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.SAME_OUTPUTS;
import static com.cobo.cold.ui.fragment.main.PsbtTxConfirmFragment.showExportPsbtDialog;
import static com.cobo.cold.viewmodel.TxConfirmViewModel.STATE_NONE;

public class UnsignedTxFragment extends BaseFragment<ElectrumTxConfirmFragmentBinding> {

    private final Runnable forgetPassword = () -> {
        Bundle data = new Bundle();
        data.putInt(KEY_NAV_ID, R.id.action_to_setPasswordFragment1);
        Navigation.findNavController(Objects.requireNonNull(getView()))
                .navigate(R.id.action_to_verifyMnemonic, data);
    };
    protected TxConfirmViewModel viewModel;
    private SigningDialog signingDialog;
    private TxEntity txEntity;
    private ModalDialog addingAddressDialog;
    private List<String> changeAddress = new ArrayList<>();
    private int feeAttackCheckingState;
    private FeeAttackChecking feeAttackChecking;
    private boolean signed;

    @Override
    protected int setView() {
        return R.layout.electrum_tx_confirm_fragment;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.txDetail.txIdInfo.setVisibility(View.GONE);
        mBinding.txDetail.export.setVisibility(View.GONE);
        mBinding.txDetail.qr.setVisibility(View.GONE);

        String walletName = WatchWallet.getWatchWallet(mActivity)
                .getWalletName(mActivity);
        mBinding.txDetail.watchWallet.setText(walletName);

        viewModel = ViewModelProviders.of(this).get(TxConfirmViewModel.class);
        mBinding.setViewModel(viewModel);
        ViewModelProviders.of(mActivity)
                .get(GlobalViewModel.class)
                .getChangeAddress()
                .observe(this, address -> this.changeAddress = address);
        subscribeTxEntityState();
        mBinding.sign.setOnClickListener(v -> handleSign());
    }

    private void handleSign() {
        if (feeAttackCheckingState == SAME_OUTPUTS) {
            feeAttackChecking.showFeeAttackWarning();
            return;
        }
        if (signed) {
            ModalDialog.showCommonModal(mActivity,getString(R.string.broadcast_tx),
                    getString(R.string.multisig_already_signed), getString(R.string.know), null);
            return;
        }

        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();

        if (txEntity != null) {
            if (FeatureFlags.ENABLE_WHITE_LIST) {
                if (isAddressInWhiteList()) {
                    AuthenticateModal.show(mActivity,
                            getString(R.string.password_modal_title),
                            "",
                            fingerprintSignEnable,
                            signWithVerifyInfo(), forgetPassword);
                } else {
                    Utilities.alert(mActivity, getString(R.string.hint),
                            getString(R.string.not_in_whitelist_reject),
                            getString(R.string.confirm),
                            () -> navigate(R.id.action_to_home));
                }

            } else {
                AuthenticateModal.show(mActivity,
                        getString(R.string.password_modal_title),
                        "",
                        fingerprintSignEnable,
                        signWithVerifyInfo(), forgetPassword);
            }
        } else {
            navigate(R.id.action_to_home);
        }
    }

    protected AuthenticateModal.OnVerify signWithVerifyInfo() {
        return token -> {
            viewModel.setToken(token);
            viewModel.handleSign();
            subscribeSignState();
        };
    }

    protected void parseTx() {
    }

    private void subscribeTxEntityState() {
        ProgressModalDialog dialog = new ProgressModalDialog();
        dialog.show(mActivity.getSupportFragmentManager(), "");
        parseTx();
        viewModel.getObservableTx().observe(this, txEntity -> {
            if (txEntity != null) {
                dialog.dismiss();
                this.txEntity = txEntity;
                mBinding.setTx(txEntity);
                refreshUI();
            }
        });
        observeParseTx(dialog);
    }

    private void refreshUI() {
        refreshAmount();
        refreshFromList();
        refreshReceiveList();
        refreshSignStatus();
    }

    private void refreshSignStatus() {
        if (!TextUtils.isEmpty(txEntity.getSignStatus())) {
            mBinding.txDetail.txSignStatus.setVisibility(View.VISIBLE);
            String signStatus = txEntity.getSignStatus();

            String[] splits = signStatus.split("-");
            int sigNumber = Integer.parseInt(splits[0]);
            int reqSigNumber = Integer.parseInt(splits[1]);

            String text;
            if (sigNumber == 0) {
                text = getString(R.string.unsigned);
            } else if(sigNumber < reqSigNumber) {
                text = getString(R.string.partial_signed);
            } else {
                text = getString(R.string.signed);
                signed = true;
            }

            mBinding.txDetail.signStatus.setText(text);
        } else {
            mBinding.txDetail.txSource.setVisibility(View.VISIBLE);
        }
    }

    private void observeParseTx(ProgressModalDialog dialog) {
        viewModel.parseTxException().observe(this, ex -> {
            if (ex != null) {
                ex.printStackTrace();
                dialog.dismiss();
                String title = getString(R.string.electrum_decode_txn_fail);
                String errorMessage = getString(R.string.incorrect_tx_data);
                String buttonText = getString(R.string.confirm);
                if (ex instanceof XpubNotMatchException || ex instanceof WatchWalletNotMatchException) {
                    errorMessage = getString(R.string.master_pubkey_not_match);
                }
                boolean multisig = false;
                if (ex instanceof InvalidTransactionException) {
                    InvalidTransactionException e = (InvalidTransactionException) ex;
                    if (e.getErrorCode() == InvalidTransactionException.IS_NOTMULTISIG_TX) {
                        title = getString(R.string.open_int_siglesig_wallet);
                        errorMessage = getString(R.string.open_int_siglesig_wallet_hint);
                        multisig = true;
                    } else if (e.getErrorCode() == InvalidTransactionException.IS_MULTISIG_TX) {
                        title = getString(R.string.open_int_multisig_wallet);
                        errorMessage = getString(R.string.open_int_multisig_wallet_hint);
                    }
                    buttonText = getString(R.string.know);
                }

                if (ex instanceof NoMatchedMultisigWallet) {
                    title = getString(R.string.no_matched_wallet);
                    errorMessage = getString(R.string.no_matched_wallet_hint);
                    buttonText = getString(R.string.know);
                    multisig = true;
                }
                ModalDialog.showCommonModal(mActivity,
                        title,
                        errorMessage,
                        buttonText,null);
                navigateUp();
            }
        });

        viewModel.feeAttackChecking().observe(this, state -> {
            feeAttackCheckingState = state;
            if (state != NORMAL) {
                feeAttackChecking = new FeeAttackChecking(this);
            }
        });
    }

    private void observeAddAddress() {
        viewModel.getAddingAddressState().observe(this, b -> {
            if (b) {
                addingAddressDialog = ModalDialog.newInstance();
                ProgressModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.progress_modal, null, false);
                binding.text.setText(R.string.sync_in_progress);
                binding.text.setVisibility(View.VISIBLE);
                addingAddressDialog.setBinding(binding);
                addingAddressDialog.show(mActivity.getSupportFragmentManager(), "");
            } else {
                if (addingAddressDialog != null) {
                    addingAddressDialog.dismiss();
                }
            }
        });
    }

    private void refreshAmount() {
        SpannableStringBuilder style = new SpannableStringBuilder(txEntity.getAmount());
        style.setSpan(new ForegroundColorSpan(mActivity.getColor(R.color.colorAccent)),
                0, txEntity.getAmount().indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.txDetail.amount.setText(style);
    }

    private void refreshReceiveList() {
        String to = txEntity.getTo();
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray outputs = new JSONArray(to);
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject output = outputs.getJSONObject(i);
                boolean isChange = output.optBoolean("isChange");
                String changePath = null;
                if (isChange) {
                    changePath = output.getString("changeAddressPath");
                }

                items.add(new TransactionItem(i,
                        output.getLong("value"),
                        output.getString("address"),
                        txEntity.getCoinCode(),changePath));
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter
                = new TransactionItemAdapter(mActivity,
                TransactionItem.ItemType.OUTPUT,
                changeAddress);
        adapter.setItems(items);
        mBinding.txDetail.toList.setVisibility(View.VISIBLE);
        mBinding.txDetail.toList.setAdapter(adapter);
    }

    private void refreshFromList() {
        String from = txEntity.getFrom();
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray inputs = new JSONArray(from);
            for (int i = 0; i < inputs.length(); i++) {
                JSONObject out = inputs.getJSONObject(i);
                items.add(new TransactionItem(i,
                        out.getLong("value"),
                        out.getString("address"),
                        txEntity.getCoinCode()));
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter
                = new TransactionItemAdapter(mActivity,
                TransactionItem.ItemType.INPUT);
        adapter.setItems(items);
        mBinding.txDetail.fromList.setVisibility(View.VISIBLE);
        mBinding.txDetail.fromList.setAdapter(adapter);
    }

    protected void subscribeSignState() {
        viewModel.getSignState().observe(this, s -> {
            if (TxConfirmViewModel.STATE_SIGNING.equals(s)) {
                signingDialog = SigningDialog.newInstance();
                signingDialog.show(mActivity.getSupportFragmentManager(), "");
            } else if (TxConfirmViewModel.STATE_SIGN_SUCCESS.equals(s)) {
                if (signingDialog != null) {
                    signingDialog.setState(SigningDialog.STATE_SUCCESS);
                }
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    onSignSuccess();
                    viewModel.getSignState().setValue(STATE_NONE);
                }, 500);
            } else if (TxConfirmViewModel.STATE_SIGN_FAIL.equals(s)) {
                if (signingDialog == null) {
                    signingDialog = SigningDialog.newInstance();
                    signingDialog.show(mActivity.getSupportFragmentManager(), "");
                }
                new Handler().postDelayed(() -> signingDialog.setState(SigningDialog.STATE_FAIL), 1000);
                new Handler().postDelayed(() -> {
                    if (signingDialog != null) {
                        signingDialog.dismiss();
                    }
                    signingDialog = null;
                    viewModel.getSignState().removeObservers(this);
                    viewModel.getSignState().setValue(STATE_NONE);
                }, 2000);
            }
        });
    }

    protected void onSignSuccess() {
        handleSignSuccess();
        viewModel.getSignState().removeObservers(this);
    }

    private void handleSignSuccess() {
        String hex = viewModel.getTxHex();
        String base43 = Base43.encode(Hex.decode(hex));
        if (base43.length() <= 1000) {
            String txId = viewModel.getTxId();
            Bundle data = new Bundle();
            data.putString(KEY_TXID, txId);
            navigate(R.id.action_to_broadcastElectrumTxFragment, data);
        } else {
            showExportPsbtDialog(mActivity, viewModel.getSignedTxEntity(), this::navigateUp);
        }
    }

    private boolean isAddressInWhiteList() {
        String to = txEntity.getTo();
        String encryptedAddress = ByteFormatter.bytes2hex(
                new KeyStoreUtil().encrypt(to.getBytes(StandardCharsets.UTF_8)));
        return viewModel.isAddressInWhiteList(encryptedAddress);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}



