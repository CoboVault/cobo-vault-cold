
/*
 *
 *  Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cobo.cold.ui.fragment.main.xumm;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.exception.InvalidAccountException;
import com.cobo.cold.R;
import com.cobo.cold.callables.FingerprintPolicyCallable;
import com.cobo.cold.databinding.XummTxConfirmBinding;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.fragment.setup.PreImportFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.ui.modal.SigningDialog;
import com.cobo.cold.ui.views.AuthenticateModal;
import com.cobo.cold.viewmodel.TxConfirmViewModel;
import com.cobo.cold.viewmodel.XummTxConfirmViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import static com.cobo.cold.callables.FingerprintPolicyCallable.READ;
import static com.cobo.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.cobo.cold.ui.fragment.main.BroadcastTxFragment.KEY_TXID;
import static com.cobo.cold.ui.fragment.main.TxConfirmFragment.KEY_TX_DATA;
import static com.cobo.cold.ui.fragment.setup.PreImportFragment.ACTION;

public class XummTxConfirmFragment extends BaseFragment<XummTxConfirmBinding> {

    private SigningDialog signingDialog;
    private XummTxConfirmViewModel viewModel;
    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };
    private JSONObject tx;

    @Override
    protected int setView() {
        return R.layout.xumm_tx_confirm;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle bundle = Objects.requireNonNull(getArguments());
        viewModel = ViewModelProviders.of(this).get(XummTxConfirmViewModel.class);
        try {
            tx = new JSONObject(bundle.getString(KEY_TX_DATA));
            viewModel.parseTxException().observe(this, this::handleParseException);
            viewModel.parseXummTxData(tx);
            viewModel.getDisplayJson().observe(this, tx -> mBinding.container.setData(tx));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBinding.sign.setOnClickListener( v -> handleSign());
    }



    private void handleSign() {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSignXummTransaction();
                    subscribeSignState();
                }, forgetPassword);
    }

    private void handleParseException(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            if (ex instanceof InvalidAccountException) {
                ModalDialog.showCommonModal(mActivity,
                        getString(R.string.xrp_account_not_match),
                        getString(R.string.xrp_account_not_match_detail) ,
                        getString(R.string.confirm),
                        null);
            } else {
                ModalDialog.showCommonModal(mActivity,
                        getString(R.string.scan_failed),
                        getString(R.string.incorrect_tx_data),
                        getString(R.string.confirm),
                        null);
            }
            popBackStack(R.id.assetFragment, false);
        }
    }

    private void subscribeSignState() {
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
                }, 2000);
            }
        });
    }

    private void onSignSuccess() {
        String txId = viewModel.getTxId();
        Bundle data = new Bundle();
        data.putString(KEY_TXID, txId);
        navigate(R.id.action_to_broadcastXummTxFragment, data);
        viewModel.getSignState().removeObservers(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
