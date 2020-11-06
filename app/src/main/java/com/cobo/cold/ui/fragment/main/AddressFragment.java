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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.cold.R;
import com.cobo.cold.databinding.AddressFragmentBinding;
import com.cobo.cold.db.entity.AddressEntity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.util.Keyboard;
import com.cobo.cold.viewmodel.CoinViewModel;
import com.cobo.cold.viewmodel.WatchWallet;

import java.util.List;
import java.util.Objects;

import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS;
import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS_NAME;
import static com.cobo.cold.ui.fragment.Constants.KEY_ADDRESS_PATH;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.cobo.cold.ui.fragment.Constants.KEY_ID;

public class AddressFragment extends BaseFragment<AddressFragmentBinding> {

    String query;
    private CoinViewModel viewModel;
    private final AddressCallback mAddrCallback = new AddressCallback() {
        @Override
        public void onClick(AddressEntity addr) {
            if (mAddressAdapter.isEditing()) {
                mAddressAdapter.exitEdit();
            } else {
                Bundle bundle = Objects.requireNonNull(getArguments());
                Bundle data = new Bundle();
                data.putString(KEY_COIN_CODE, bundle.getString(KEY_COIN_CODE));
                data.putString(KEY_ADDRESS, addr.getAddressString());
                data.putString(KEY_ADDRESS_NAME, addr.getName());
                data.putString(KEY_ADDRESS_PATH, addr.getPath());
                navigate(R.id.action_to_receiveCoinFragment, data);
            }
        }

        @Override
        public void onNameChange(AddressEntity addr) {
            viewModel.updateAddress(addr);
        }
    };

    private AddressAdapter mAddressAdapter;

    public void exitEditAddressName() {
        if (mAddressAdapter.isEditing()) {
            mAddressAdapter.exitEdit();
        }
    }

    public static Fragment newInstance(long id, @NonNull String coinId, @NonNull String coinCode) {
        AddressFragment fragment = new AddressFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_ID, id);
        args.putString(KEY_COIN_ID, coinId);
        args.putString(KEY_COIN_CODE, coinCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int setView() {
        return R.layout.address_fragment;
    }

    @Override
    protected void init(View view) {
        mAddressAdapter = new AddressAdapter(mActivity, mAddrCallback);
        mBinding.addrList.setAdapter(mAddressAdapter);
        mAddressAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (!TextUtils.isEmpty(query) && mAddressAdapter.getItemCount() == 0) {
                    mBinding.empty.setVisibility(View.VISIBLE);
                    mBinding.addrList.setVisibility(View.GONE);
                } else {
                    mBinding.empty.setVisibility(View.GONE);
                    mBinding.addrList.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle data = Objects.requireNonNull(getArguments());
        Objects.requireNonNull(getParentFragment());
        CoinViewModel.Factory factory = new CoinViewModel.Factory(mActivity.getApplication(),
                data.getLong(KEY_ID),
                data.getString(KEY_COIN_ID));
        viewModel = ViewModelProviders.of(getParentFragment(), factory)
                .get(CoinViewModel.class);
        subscribeUi(viewModel.getAddress());

    }

    private void subscribeUi(LiveData<List<AddressEntity>> address) {
        address.observe(this, entities -> {
            if (WatchWallet.getWatchWallet(mActivity) == WatchWallet.XRP_TOOLKIT) {
                mAddressAdapter.setItems(entities.subList(0,1));
            } else {
                mAddressAdapter.setItems(entities);
            }
        });
    }

    public void setQuery(String s) {
        query = s;
        mAddressAdapter.getFilter().filter(s);
    }

    public void enterSearch() {
        if (mAddressAdapter != null) {
            mAddressAdapter.enterSearch();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Keyboard.hide(mActivity, Objects.requireNonNull(getView()));
    }
}
