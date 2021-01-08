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
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.coins.ETH.Network;
import com.cobo.cold.R;
import com.cobo.cold.databinding.AbiItemBinding;
import com.cobo.cold.databinding.EthTxBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.cobo.coinlib.v8.ScriptLoader.readAsset;
import static com.cobo.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

public class EthTxFragment extends BaseFragment<EthTxBinding> {

    @Override
    protected int setView() {
        return R.layout.eth_tx;
    }

    @Override
    protected void init(View view) {
        Bundle bundle = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        mBinding.broadcastHint.setText(getString(R.string.please_broadcast_with_hot, WatchWallet.METAMASK.getWalletName(mActivity)));
        viewModel.loadTx(bundle.getString(KEY_TX_ID)).observe(this, txEntity -> {
            if (txEntity != null) {
                updateUI(txEntity);
            }
        });
    }

    private void updateUI(TxEntity txEntity) {
        int chainId = 1;
        JSONObject signed = null;
        JSONObject abi = null;
        try {
            signed = new JSONObject(txEntity.getSignedHex());
            chainId = signed.optInt("chainId",1);
            abi = signed.getJSONObject("abi");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBinding.ethTx.network.setText(getNetwork(chainId));
        if (signed != null) {
            signed.remove("abi");
            signed.remove("chainId");
            mBinding.qrcode.qrcode.setData(Hex.toHexString(signed.toString().getBytes(StandardCharsets.UTF_8)));
        }
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
        String to = txEntity.getTo();
        try {
            String contract = null;
            JSONArray tokensMap = new JSONArray(readAsset(mActivity.getAssets(), "abi/token_contract_address.json"));
            for (int i = 0; i < tokensMap.length(); i++) {
                JSONObject token = tokensMap.getJSONObject(i);
                if (token.getString("contract_address").equalsIgnoreCase(to)) {
                    contract = token.getString("name");
                    break;
                }
            }

            if (contract == null) {
                JSONObject bundleMap = new JSONObject(readAsset(mActivity.getAssets(), "abi/abiMap.json"));
                String abiFile = bundleMap.optString(txEntity.getTo());
                if (!TextUtils.isEmpty(abiFile)) {
                    contract = abiFile.replace(".json", "");
                }
            }
            if (contract != null) {
                to = to + "\n" + String.format("(%s)", contract);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mBinding.ethTx.to.setText(to);
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
