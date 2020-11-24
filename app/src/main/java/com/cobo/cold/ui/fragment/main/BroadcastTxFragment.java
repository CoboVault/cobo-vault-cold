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
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.databinding.BroadcastTxFragmentBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.protocol.builder.SignTxResultBuilder;
import com.cobo.cold.ui.BindingAdapters;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class BroadcastTxFragment extends BaseFragment<BroadcastTxFragmentBinding> {

    public static final String KEY_TXID = "txId";

    private WatchWallet watchWallet;

    protected TxEntity txEntity;

    private final View.OnClickListener goHome = v -> navigate(R.id.action_to_home);

    @Override
    protected int setView() {
        return R.layout.broadcast_tx_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        mBinding.toolbar.setNavigationOnClickListener(goHome);
        mBinding.complete.setOnClickListener(goHome);

        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
            mBinding.setCoinCode(txEntity.getCoinCode());
            this.txEntity = txEntity;
            refreshUI();
            mBinding.qrcodeLayout.qrcode.setData(getSignedTxData());
        });
    }

    private void refreshUI() {
        mBinding.broadcastHint.setText(getString(R.string.please_broadcast_with_hot,
                watchWallet.getWalletName(mActivity)));
        mBinding.info.setOnClickListener(v -> showBroadcastHint());
        if (watchWallet == WatchWallet.POLKADOT_JS) {
            mBinding.qrcodeLayout.qrcode.disableMultipart();
        }
        refreshTokenUI();
    }

    private void showBroadcastHint() {
        ModalDialog.showCommonModal(mActivity,"广播指引","广播指引", getString(R.string.know), null);
    }

    private void refreshTokenUI() {
        String assetCode = null;
        try {
            assetCode = txEntity.getAmount().split(" ")[1];
        } catch (Exception ignore) {
        }
        if (TextUtils.isEmpty(assetCode)) {
            assetCode = txEntity.getCoinCode();
        }
        BindingAdapters.setIcon(mBinding.icon,
                txEntity.getCoinCode(),
                assetCode);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    public String getSignedTxData() {
        if (watchWallet == WatchWallet.COBO) {
            return getSignTxJson(txEntity);
        } else if(watchWallet == WatchWallet.POLKADOT_JS) {
            try {
                return new JSONObject(txEntity.getSignedHex())
                        .getString("signedHex");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    protected String getSignTxJson(TxEntity txEntity) {
        SignTxResultBuilder signTxResult = new SignTxResultBuilder();
        signTxResult.setRawTx(txEntity.getSignedHex())
                .setSignId(txEntity.getSignId())
                .setTxId(txEntity.getTxId());
        return signTxResult.build();
    }
}
