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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.TxListBinding;
import com.cobo.cold.databinding.TxListItemBinding;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.ui.common.FilterableBaseBindingAdapter;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.CoinListViewModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.cobo.cold.ui.fragment.main.TxFragment.KEY_TX_ID;
import static com.cobo.cold.viewmodel.ElectrumViewModel.ELECTRUM_SIGN_ID;

public class TxListFragment extends BaseFragment<TxListBinding> {

    private TxAdapter adapter;
    private TxCallback txCallback;
    private String query;
    private Comparator<TxEntity> txEntityComparator;


    static Fragment newInstance(@NonNull String coinId, @NonNull String coinCode) {
        TxListFragment fragment = new TxListFragment();
        Bundle args = new Bundle();
        args.putString(KEY_COIN_ID, coinId);
        args.putString(KEY_COIN_CODE, coinCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.tx_list;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        CoinListViewModel viewModel = ViewModelProviders.of(mActivity)
                .get(CoinListViewModel.class);
        adapter = new TxAdapter(mActivity);
        mBinding.list.setAdapter(adapter);
        txCallback = tx -> {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_TX_ID, tx.getTxId());
            if (ELECTRUM_SIGN_ID.equals(tx.getSignId())) {
                Navigation.findNavController(Objects.requireNonNull(getView()))
                        .navigate(R.id.action_to_electrumTxFragment, bundle);
            } else {
                Navigation.findNavController(Objects.requireNonNull(getView()))
                        .navigate(R.id.action_to_txFragment, bundle);
            }
        };

        viewModel.loadTxs(data.getString(KEY_COIN_ID))
                .observe(this, txEntities -> {
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
                            .sorted(txEntityComparator)
                            .collect(Collectors.toList());
                    adapter.setItems(txEntities);
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
            updateFrom(binding, item);
            updateTo(binding, item);
        }

        private void updateTo(TxListItemBinding binding, TxEntity item) {
            String to = item.getTo();
            binding.to.setText(item.getTo());
            try {
                JSONArray outputs = new JSONArray(to);
                binding.to.setText(outputs.getJSONObject(0).getString("address"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void updateFrom(TxListItemBinding binding, TxEntity item) {
            String from = item.getFrom();
            binding.from.setText(item.getFrom());
            try {
                JSONArray inputs = new JSONArray(from);
                String address = inputs.getJSONObject(0).getString("address");
                binding.from.setText(address);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}


