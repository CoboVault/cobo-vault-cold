
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
import android.view.View;

import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.coins.XRP.xumm.SupportTransactions;
import com.cobo.cold.R;
import com.cobo.cold.databinding.XummTxBinding;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import static com.cobo.cold.ui.fragment.main.TxFragment.KEY_TX_ID;

public class XummTxFragment extends BaseFragment<XummTxBinding> {

    @Override
    protected int setView() {
        return R.layout.xumm_tx;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        Bundle data = Objects.requireNonNull(getArguments());

        CoinListViewModel viewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        viewModel.loadTx(data.getString(KEY_TX_ID)).observe(this, txEntity -> {
            try {
                JSONObject jsonObject = new JSONObject(txEntity.getSignedHex());
                String txHex = jsonObject.getString("txHex");
                jsonObject.remove("txHex");
                mBinding.info.setOnClickListener(v -> showBroadcastHint());
                mBinding.broadcastHint.setText(getString(R.string.please_broadcast_with_hot,
                        WatchWallet.getWatchWallet(mActivity)));
                mBinding.qrcode.qrcode.setData(txHex);
                mBinding.container.setData(SupportTransactions.get(jsonObject.getString("TransactionType"))
                        .flatTransactionDetail(jsonObject));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void showBroadcastHint() {
        ModalDialog.showCommonModal(mActivity,"广播指引","广播指引", getString(R.string.know), null);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
