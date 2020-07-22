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

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.TxListBinding;
import com.cobo.cold.databinding.TxListItemBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.common.FilterableBaseBindingAdapter;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.MultiSigViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.cobo.cold.ui.fragment.main.TxFragment.KEY_TX_ID;
import static com.cobo.cold.viewmodel.WatchWallet.ELECTRUM_SIGN_ID;
import static com.cobo.cold.viewmodel.WatchWallet.PSBT_MULTISIG_SIGN_ID;
import static com.cobo.cold.viewmodel.WatchWallet.getWatchWallet;

public class TxListFragment extends BaseFragment<TxListBinding> {

    private TxAdapter adapter;
    private TxCallback txCallback;
    private String query;
    private Comparator<TxEntity> txEntityComparator;
    private boolean multisig;
    private String walletFingerprint;

    @Override
    protected int setView() {
        return R.layout.tx_list;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        multisig = data.getBoolean("multisig");
        if (multisig) {
            walletFingerprint = data.getString("wallet_fingerprint");
        }
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity)
                .get(CoinListViewModel.class);
        adapter = new TxAdapter(mActivity);
        mBinding.list.setAdapter(adapter);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        txCallback = tx -> {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_TX_ID, tx.getTxId());
            if (getWatchWallet(mActivity).supportPsbt()) {
                navigate(R.id.action_to_psbtSignedTxFragment, bundle);
            } else if (ELECTRUM_SIGN_ID.equals(tx.getSignId()) || PSBT_MULTISIG_SIGN_ID.equals(tx.getSignId())) {
                navigate(R.id.action_to_electrumTxFragment, bundle);
            } else {
                navigate(R.id.action_to_txFragment, bundle);
            }
        };

        LiveData<List<TxEntity>> txs;
        if (multisig) {
            MultiSigViewModel vm = ViewModelProviders.of(this).get(MultiSigViewModel.class);
            txs = vm.loadTxs(walletFingerprint);
        } else {
            txs = viewModel.loadTxs(data.getString(KEY_COIN_ID));
        }
        txs.observe(this, txEntities -> {
            if (!multisig) {
                txEntityComparator = (o1, o2) -> {
                    if (o1.getSignId().equals(o2.getSignId())) {
                        return (int) (o2.getTimeStamp() - o1.getTimeStamp());
                    } else if (ELECTRUM_SIGN_ID.equals(o1.getSignId())) {
                        return -1;
                    } else {
                        return 1;
                    }
                };
                txEntities = txEntities.stream()
                        .filter(this::shouldShow)
                        .filter(this::filterByMode)
                        .sorted(txEntityComparator)
                        .collect(Collectors.toList());
            }

            if (txEntities.isEmpty()) {
                showEmpty(true);
            } else {
                showEmpty(false);
                adapter.setItems(txEntities);
            }
        });
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (!TextUtils.isEmpty(query) && adapter.getItemCount() == 0) {
                    mBinding.empty.setVisibility(View.VISIBLE);
                    mBinding.list.setVisibility(View.GONE);
                } else {
                    mBinding.empty.setVisibility(View.GONE);
                    mBinding.list.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showEmpty(boolean empty) {
        if (empty) {
            mBinding.list.setVisibility(View.GONE);
            mBinding.txid.setVisibility(View.GONE);
            mBinding.empty.setVisibility(View.VISIBLE);
        } else {
            mBinding.list.setVisibility(View.VISIBLE);
            mBinding.txid.setVisibility(View.VISIBLE);
            mBinding.empty.setVisibility(View.GONE);
        }
    }

    private boolean filterByMode(TxEntity txEntity) {
        WatchWallet watchWallet = getWatchWallet(mActivity);
        if (watchWallet == WatchWallet.COBO) {
            return !txEntity.getSignId().endsWith("_sign_id");
        } else {
            return watchWallet.getSignId().equals(txEntity.getSignId());
        }
    }

    private boolean shouldShow(TxEntity tx) {
        return Utilities.getCurrentBelongTo(mActivity).equals(tx.getBelongTo());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
    }

    public void setQuery(String s) {
        if (adapter != null) {
            query = s;
            adapter.getFilter().filter(s);
        }
    }

    class TxAdapter extends FilterableBaseBindingAdapter<TxEntity, TxListItemBinding> {

        TxAdapter(Context context) {
            super(context);
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.tx_list_item;
        }

        @Override
        protected void onBindItem(TxListItemBinding binding, TxEntity item) {
            binding.setTx(item);
            binding.setTxCallback(txCallback);

        }
    }

}


