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

package com.cobo.cold.ui.fragment.main;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cobo.coinlib.coins.ETH.Network;
import com.cobo.cold.R;
import com.cobo.cold.callables.FingerprintPolicyCallable;
import com.cobo.cold.databinding.AbiItemBinding;
import com.cobo.cold.databinding.EthTxConfirmBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.fragment.setup.PreImportFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.ui.modal.SigningDialog;
import com.cobo.cold.ui.views.AuthenticateModal;
import com.cobo.cold.viewmodel.EthTxConfirmViewModel;
import com.cobo.cold.viewmodel.TxConfirmViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

import static com.cobo.cold.callables.FingerprintPolicyCallable.READ;
import static com.cobo.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.cobo.cold.ui.fragment.main.BroadcastTxFragment.KEY_TXID;
import static com.cobo.cold.ui.fragment.main.TxConfirmFragment.KEY_TX_DATA;
import static com.cobo.cold.ui.fragment.setup.PreImportFragment.ACTION;

public class EthTxConfirmFragment extends BaseFragment<EthTxConfirmBinding> {

    private EthTxConfirmViewModel viewModel;
    private SigningDialog signingDialog;
    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };

    @Override
    protected int setView() {
        return R.layout.eth_tx_confirm;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        viewModel = ViewModelProviders.of(this).get(EthTxConfirmViewModel.class);
        try {
            JSONObject txData = new JSONObject(data.getString(KEY_TX_DATA));
            viewModel.parseTxData(txData);
            viewModel.getObservableTx().observe(this, this::updateUI);
            viewModel.parseTxException().observe(this, this::handleParseException);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBinding.sign.setOnClickListener(v -> handleSign());
    }

    private void handleParseException(Exception ex) {
        if (ex != null) {
            ex.printStackTrace();
            ModalDialog.showCommonModal(mActivity,
                    getString(R.string.scan_failed),
                    getString(R.string.incorrect_tx_data),
                    getString(R.string.confirm),
                    null);
            viewModel.parseTxException().setValue(null);
            popBackStack(R.id.assetFragment, false);
        }
    }

    private void handleSign() {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSign();
                    subscribeSignState();
                }, forgetPassword);
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
        navigate(R.id.action_to_ethBroadcastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }

    private void updateUI(TxEntity txEntity) {
        mBinding.ethTx.network.setText(getNetwork(viewModel.getChainId()));
        JSONObject abi = viewModel.getAbi();
        if (abi != null) {
            List<AbiItemAdapter.AbiItem> itemList = new AbiItemAdapter().adapt(abi);
            for (AbiItemAdapter.AbiItem item : itemList) {
                AbiItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.abi_item, null, false);
                binding.key.setText(item.key);
                binding.value.setText(item.value);
                mBinding.ethTx.container.addView(binding.getRoot());
            }
        }
        mBinding.ethTx.setTx(txEntity);
    }

    private String getNetwork(int chainId) {
        String network = Network.getNetwork(chainId).name();
        if (chainId != 1) {
            network += String.format("(%s)",getString(R.string.testnet));
        }
        return network;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
