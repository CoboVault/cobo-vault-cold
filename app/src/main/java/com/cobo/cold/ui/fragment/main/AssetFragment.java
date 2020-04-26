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
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.R;
import com.cobo.cold.databinding.AssetFragmentBinding;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ProgressModalDialog;
import com.cobo.cold.viewmodel.AddAddressViewModel;
import com.cobo.cold.viewmodel.CoinViewModel;
import com.cobo.cold.viewmodel.PublicKeyViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.cobo.cold.ui.fragment.Constants.KEY_ID;

public class AssetFragment extends BaseFragment<AssetFragmentBinding>
        implements Toolbar.OnMenuItemClickListener, NumberPickerCallback {

    private final ObservableField<String> query = new ObservableField<>();

    private boolean isInSearch;
    private Fragment[] fragments;
    private boolean showPublicKey;
    private String coinId;
    private String coinCode;
    private long id;
    private AddressNumberPicker mAddressNumberPicker;
    private boolean hasAddress;

    @Override
    protected int setView() {
        return R.layout.asset_fragment;
    }

    @Override
    protected void init(View view) {
        Bundle data = Objects.requireNonNull(getArguments());
        coinId = data.getString(KEY_COIN_ID);
        coinCode = data.getString(KEY_COIN_CODE);
        id = data.getLong(KEY_ID);
        showPublicKey = Coins.showPublicKey(coinCode);
        mBinding.toolbar.inflateMenu(showPublicKey ? R.menu.asset_without_add : R.menu.asset);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        initSearchView();
        initTabs();
    }

    private void initTabs() {
        if (!showPublicKey) {
            initViewPager();
        } else {
            PublicKeyViewModel viewModel = ViewModelProviders.of(this)
                    .get(PublicKeyViewModel.class);
            Handler handler = new Handler();
            AppExecutors.getInstance().diskIO().execute(() -> {
                String address = viewModel.getAddress(coinId);
                hasAddress = !TextUtils.isEmpty(address);
                handler.post(this::initViewPager);

            });
        }

    }

    private void initViewPager() {
        String[] title = {showPublicKey && !hasAddress ? getString(R.string.tab_my_pubkey)
                : getString(R.string.tab_my_address),
                getString(R.string.tab_transaction_history)};
        if (fragments == null) {
            fragments = new Fragment[title.length];
            if (showPublicKey) {
                fragments[0] = PublicKeyFragment.newInstance(coinId);
            } else {
                fragments[0] = AddressFragment.newInstance(id, coinId, coinCode);
            }
            fragments[1] = TxListFragment.newInstance(coinId, coinCode);
        }

        mBinding.viewpager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return title.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mBinding.tab.setupWithViewPager(mBinding.viewpager);
    }

    private void initSearchView() {
        mBinding.btnCancel.setOnClickListener(v -> exitSearch());
        View.OnKeyListener backListener = (view, key_code, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (key_code == KeyEvent.KEYCODE_BACK) {
                    if (isInSearch) {
                        exitSearch();
                        return true;
                    }
                }
            }
            return false;
        };
        mBinding.search.setOnKeyListener(backListener);
        query.set("");
        mBinding.setQuery(query);
        query.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                if (fragments[0] instanceof AddressFragment) {
                    AddressFragment addressFragment = (AddressFragment) fragments[0];
                    addressFragment.setQuery(query.get());
                }

                TxListFragment txListFragment = (TxListFragment) fragments[1];
                txListFragment.setQuery(query.get());

            }
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CoinViewModel.Factory factory = new CoinViewModel.Factory(mActivity.getApplication(), id, coinId);
        CoinViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(CoinViewModel.class);

        mBinding.setCoinViewModel(viewModel);
        subscribeUi(viewModel);
    }

    private void subscribeUi(CoinViewModel viewModel) {
        viewModel.getObservableCoin().observe(this, viewModel::setCoin);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                enterSearch();
                break;
            case R.id.action_add:
                if (fragments[0] instanceof AddressFragment) {
                    ((AddressFragment) fragments[0]).exitEditAddressName();
                }
                if (mAddressNumberPicker == null) {
                    mAddressNumberPicker = new AddressNumberPicker();
                    mAddressNumberPicker.setCallback(this);
                }
                mAddressNumberPicker.show(mActivity.getSupportFragmentManager(), "");
                break;
            default:
                break;
        }
        return true;
    }

    private void enterSearch() {
        isInSearch = true;
        if (fragments[0] != null && fragments[0] instanceof AddressFragment) {
            ((AddressFragment) fragments[0]).enterSearch();
        }
        mBinding.searchBar.setVisibility(View.VISIBLE);
        mBinding.search.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(mBinding.search, 0);
        }
    }

    private void exitSearch() {
        isInSearch = false;
        mBinding.search.setText("");
        mBinding.searchBar.setVisibility(View.INVISIBLE);
        mBinding.search.clearFocus();
        InputMethodManager inputManager =
                (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(mBinding.search.getWindowToken(), 0);
        }
    }

    @Override
    public void onValueSet(int value) {
        AddAddressViewModel.Factory factory = new AddAddressViewModel.Factory(mActivity.getApplication(),
                id);
        AddAddressViewModel viewModel = ViewModelProviders.of(this, factory)
                .get(AddAddressViewModel.class);

        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
        Handler handler = new Handler();
        AppExecutors.getInstance().diskIO().execute(() -> {
            CoinEntity coinEntity = viewModel.getCoin(coinId);
            if (coinEntity != null) {
                int addrCount = coinEntity.getAddressCount();
                List<String> observableAddressNames = new ArrayList<>();
                for (int i = addrCount; i < value + addrCount; i++) {
                    String name = coinEntity.getCoinCode() + "-" + (i + 1);
                    observableAddressNames.add(name);
                }
                viewModel.addAddress(observableAddressNames);

                handler.post(() -> viewModel.getObservableAddState().observe(this, complete -> {
                    if (complete) {
                        handler.postDelayed(dialog::dismiss, 500);
                    }
                }));
            }
        });
    }
}
