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
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.cobo.coinlib.utils.Base43;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.SignedTxBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.fragment.main.TransactionItem;
import com.cobo.cold.ui.fragment.main.TransactionItemAdapter;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.GlobalViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cobo.cold.ui.fragment.main.FeeAttackChecking.KEY_DUPLICATE_TX;
import static com.cobo.cold.ui.fragment.main.electrum.ElectrumBroadcastTxFragment.showElectrumInfo;
import static com.cobo.cold.ui.fragment.main.electrum.UnsignedTxFragment.showExportTxnDialog;


public class SignedTxFragment extends BaseFragment<SignedTxBinding> {

    private static final String KEY_TX_ID = "txid";
    protected TxEntity txEntity;
    private List<String> changeAddress = new ArrayList<>();
    protected WatchWallet watchWallet;

    @Override
    protected int setView() {
        return R.layout.signed_tx;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        mBinding.toolbar.setNavigationOnClickListener(v -> {
            if (data.getBoolean(KEY_DUPLICATE_TX)) {
                NavHostFragment.findNavController(this)
                        .popBackStack(R.id.assetFragment, false);
            } else {
                navigateUp();
            }
        });
        watchWallet = WatchWallet.getWatchWallet(mActivity);
        String walletName = watchWallet.getWalletName(mActivity);
        mBinding.txDetail.watchWallet.setText(walletName);

        ViewModelProviders.of(mActivity)
                .get(GlobalViewModel.class)
                .getChangeAddress()
                .observe(this, address -> this.changeAddress = address);
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TX_ID)).observe(this, txEntity -> {
            mBinding.setTx(txEntity);
            this.txEntity = txEntity;
            displaySignResult(txEntity);
            refreshAmount();
            refreshFromList();
            refreshReceiveList();
            mBinding.txDetail.exportToSdcard.setOnClickListener(v -> showExportDialog());
        });
    }

    protected void showExportDialog() {
        showExportTxnDialog(mActivity, txEntity.getTxId(), txEntity.getSignedHex(),null);
    }

    private void refreshFromList() {
        String from = txEntity.getFrom();
        List<TransactionItem> items = new ArrayList<>();
        try {
            JSONArray outputs = new JSONArray(from);
            for (int i = 0; i < outputs.length(); i++) {
                JSONObject out = outputs.getJSONObject(i);
                items.add(new TransactionItem(i,
                        out.getLong("value"), out.getString("address"),
                        Coins.BTC.coinCode()));
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter
                = new TransactionItemAdapter(mActivity,
                TransactionItem.ItemType.INPUT, changeAddress);
        adapter.setItems(items);
        mBinding.txDetail.fromList.setAdapter(adapter);
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
                items.add(new TransactionItem(i,
                        outputs.getJSONObject(i).getLong("value"),
                        outputs.getJSONObject(i).getString("address"),
                        Coins.BTC.coinCode()
                ));
            }
        } catch (JSONException e) {
            return;
        }
        TransactionItemAdapter adapter =
                new TransactionItemAdapter(mActivity,
                        TransactionItem.ItemType.OUTPUT,
                        changeAddress);
        adapter.setItems(items);
        mBinding.txDetail.toList.setAdapter(adapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    protected void displaySignResult(TxEntity txEntity) {
        byte[] txData = Hex.decode(txEntity.getSignedHex());
        String base43 = Base43.encode(txData);
        if (base43.length() <= 1000) {
            new Handler().postDelayed(() -> mBinding.txDetail.qrcodeLayout.qrcode.setData(base43), 500);
            mBinding.txDetail.export.setVisibility(View.GONE);
            mBinding.txDetail.exportToSdcardHint.setOnClickListener(v ->
                    showExportTxnDialog(mActivity, txEntity.getTxId(), txEntity.getSignedHex(), null));
            mBinding.txDetail.info.setOnClickListener(v -> showElectrumInfo(mActivity));
        } else {
            mBinding.txDetail.qr.setVisibility(View.GONE);
        }
    }

}
