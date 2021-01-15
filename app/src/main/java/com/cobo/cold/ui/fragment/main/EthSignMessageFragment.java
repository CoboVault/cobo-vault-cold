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
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.callables.FingerprintPolicyCallable;
import com.cobo.cold.databinding.EthSignMessageBinding;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.fragment.setup.PreImportFragment;
import com.cobo.cold.ui.modal.SigningDialog;
import com.cobo.cold.ui.views.AuthenticateModal;
import com.cobo.cold.viewmodel.EthTxConfirmViewModel;
import com.cobo.cold.viewmodel.TxConfirmViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cobo.cold.callables.FingerprintPolicyCallable.READ;
import static com.cobo.cold.callables.FingerprintPolicyCallable.TYPE_SIGN_TX;
import static com.cobo.cold.ui.fragment.main.EthTxConfirmFragment.highLight;
import static com.cobo.cold.ui.fragment.main.TxConfirmFragment.KEY_TX_DATA;
import static com.cobo.cold.ui.fragment.setup.PreImportFragment.ACTION;

public class EthSignMessageFragment extends BaseFragment<EthSignMessageBinding> {

    private EthTxConfirmViewModel viewModel;
    private SigningDialog signingDialog;

    private final Runnable forgetPassword = () -> {
        Bundle bundle = new Bundle();
        bundle.putString(ACTION, PreImportFragment.ACTION_RESET_PWD);
        navigate(R.id.action_to_preImportFragment, bundle);
    };

    @Override
    protected int setView() {
        return R.layout.eth_sign_message;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        viewModel = ViewModelProviders.of(this).get(EthTxConfirmViewModel.class);
        String txData = data.getString(KEY_TX_DATA);
        LiveData<JSONObject> liveData = viewModel.parseMessageData(txData);
        liveData.observe(this, o -> onMessageParsed(liveData, o));
        mBinding.sign.setOnClickListener(v -> handleSign());
    }

    private void onMessageParsed(LiveData<JSONObject> liveData, JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                JSONObject messageData = jsonObject.getJSONObject("data");
                JSONObject domain = messageData.getJSONObject("domain");
                mBinding.primaryType.setText(messageData.getString("primaryType"));
                mBinding.network.setText(viewModel.getNetwork(domain.getInt("chainId")));
                mBinding.name.setText(domain.optString("name"));
                mBinding.verifyingContract.setText(highLight(recognizeAddress(domain.getString("verifyingContract"))));
                String message = messageData.getJSONObject("message").toString(2);
                mBinding.message.setText(highLight(recognizeAddressInText(message)));
                liveData.removeObservers(EthSignMessageFragment.this);
            } catch (JSONException ignore){}
        }
    }

    private String recognizeAddress(String address) {
        String addressSymbol = viewModel.recognizeAddress(address);
        if (addressSymbol != null) {
            address = address + String.format(" (%s)", addressSymbol);
        } else {
            address = address + String.format(" [%s]", "Unknown Address");
        }
        return address;
    }

    private String recognizeAddressInText(String text) {
        Pattern pattern = Pattern.compile("0x[a-fA-F0-9]{40}");
        Matcher matcher = pattern.matcher(text);
        Map<String, String> recognized = new HashMap<>();
        Set<String> unknown = new HashSet<>();
        while (matcher.find()) {
            String address = matcher.group();
            String symbol = viewModel.recognizeAddress(address);
            if (symbol != null) {
               recognized.put(address, symbol);
            } else if (address.equalsIgnoreCase(viewModel.getFromAddress())){
                //do nothing
            }else {
                unknown.add(address);
            }
        }

        for (String s: recognized.keySet()) {
            text = text.replace(s, s+String.format(" (%s)", recognized.get(s)));
        }

        for (String s: unknown) {
            text = text.replace(s, s +" [Unknown Address]");
        }

        return text;
    }

    private void handleSign() {
        boolean fingerprintSignEnable = new FingerprintPolicyCallable(READ, TYPE_SIGN_TX).call();
        AuthenticateModal.show(mActivity,
                getString(R.string.password_modal_title), "", fingerprintSignEnable,
                token -> {
                    viewModel.setToken(token);
                    viewModel.handleSignMessage();
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
        String signature = viewModel.getMessageSignature();
        Bundle data = new Bundle();
        data.putString("MessageSignature", signature);
        navigate(R.id.action_to_ethBroadcastTxFragment, data);
        viewModel.getSignState().setValue("");
        viewModel.getSignState().removeObservers(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

}
