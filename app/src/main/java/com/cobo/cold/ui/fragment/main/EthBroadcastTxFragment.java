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
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.R;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class EthBroadcastTxFragment extends BroadcastTxFragment {
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

        String txId = data.getString(KEY_TXID);
        String messageSignature = data.getString("MessageSignature");
        if (!TextUtils.isEmpty(txId)) {
            ViewModelProviders.of(mActivity).get(CoinListViewModel.class)
                    .loadTx(data.getString(KEY_TXID)).observe(this, txEntity -> {
                mBinding.setCoinCode(txEntity.getCoinCode());
                this.txEntity = txEntity;
                refreshUI();
                mBinding.qrcodeLayout.qrcode.setData(getSignedTxData());
            });
        } else if (!TextUtils.isEmpty(messageSignature)){
            mBinding.qrcodeLayout.qrcode.setData(Hex.toHexString(messageSignature.getBytes(StandardCharsets.UTF_8)));
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> popBackStack(R.id.assetFragment,false));
        mBinding.broadcastHint.setText(R.string.sync_with_metamask);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public String getSignedTxData() {
        try {
            JSONObject signed = new JSONObject(txEntity.getSignedHex());
            signed.remove("abi");
            signed.remove("chainId");
            return Hex.toHexString(signed.toString().getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
