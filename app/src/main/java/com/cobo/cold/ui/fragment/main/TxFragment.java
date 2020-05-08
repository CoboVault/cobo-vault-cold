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
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.TxBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.protocol.builder.SignTxResultBuilder;
import com.cobo.cold.ui.BindingAdapters;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.CoinListViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class TxFragment extends BaseFragment<TxBinding> {

    public static final String KEY_TX_ID = "txid";
    private TxEntity txEntity;

    @Override
    protected int setView() {
        return R.layout.tx;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TX_ID)).observe(this, txEntity -> {
            mBinding.setTx(txEntity);
            this.txEntity = txEntity;
            new Handler().postDelayed(() -> mBinding.qrcodeLayout.qrcode.setData(getSignTxJson(txEntity)), 500);
            refreshAmount();
            refreshFromList();
            refreshReceiveList();
            refreshTokenUI();
            refreshFeeDisplay();
            refreshMemoDisplay();
        });

    }

    private void refreshMemoDisplay() {
        if (txEntity.getCoinCode().equals(Coins.EOS.coinCode())
                || txEntity.getCoinCode().equals(Coins.IOST.coinCode())) {
            mBinding.txDetail.memoLabel.setText(R.string.tag);
        }
    }

    private void refreshFeeDisplay() {
        if (txEntity.getCoinCode().equals(Coins.EOS.coinCode())
                || txEntity.getCoinCode().equals(Coins.IOST.coinCode())) {
            mBinding.txDetail.feeInfo.setVisibility(View.GONE);
        }
    }

    private void refreshAmount() {
        SpannableStringBuilder style = new SpannableStringBuilder(txEntity.getAmount());
        style.setSpan(new ForegroundColorSpan(mActivity.getColor(R.color.colorAccent)),
                0, txEntity.getAmount().indexOf(" "), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mBinding.txDetail.amount.setText(style);
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
        BindingAdapters.setIcon(mBinding.txDetail.icon,
                txEntity.getCoinCode(),
                assetCode);
        if (!assetCode.equals(txEntity.getCoinCode())) {
            mBinding.txDetail.coinId.setText(assetCode);
        } else {
            mBinding.txDetail.coinId.setText(Coins.coinNameOfCoinId(txEntity.getCoinId()));
        }
    }

    private void refreshReceiveList() {
        String to = txEntity.getTo();
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray outputs = new JSONArray(to);
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject output = outputs.getJSONObject(i);
                if (output.optBoolean("isChange")) {
                    continue;
                }
                items.add(new TransactionItem(i,
                        output.getLong("value"),
                        output.getString("address")
                ));
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter =
                new TransactionItemAdapter(mActivity,
                        TransactionItem.ItemType.TO);
        adapter.setItems(items);
        mBinding.txDetail.toList.setVisibility(View.VISIBLE);
        mBinding.txDetail.toInfo.setVisibility(View.GONE);
        mBinding.txDetail.toList.setAdapter(adapter);
    }

    private void refreshFromList() {
        String from = txEntity.getFrom();
        mBinding.txDetail.from.setText(from);
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray inputs = new JSONArray(from);
            for (int i = 0; i < inputs.length(); i++) {
                items.add(new TransactionItem(i,
                        inputs.getJSONObject(i).getLong("value"),
                        inputs.getJSONObject(i).getString("address")
                ));
            }
            String fromAddress = inputs.getJSONObject(0).getString("address");
            mBinding.txDetail.from.setText(fromAddress);
        } catch (JSONException ignore) {}
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private String getSignTxJson(TxEntity txEntity) {
        SignTxResultBuilder signTxResult = new SignTxResultBuilder();
        signTxResult.setRawTx(txEntity.getSignedHex())
                .setSignId(txEntity.getSignId())
                .setTxId(txEntity.getTxId());
        return signTxResult.build();
    }

}
