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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.cold.AppExecutors;
import com.cobo.cold.MainApplication;
import com.cobo.cold.R;
import com.cobo.cold.config.FeatureFlags;
import com.cobo.cold.databinding.ManageCoinFragmentBinding;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.CoinListViewModel;

import java.util.List;
import java.util.Objects;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;
import static com.cobo.cold.Utilities.IS_SET_PASSPHRASE;
import static com.cobo.cold.viewmodel.CoinListViewModel.coinEntityComparator;

public class ManageCoinFragment extends BaseFragment<ManageCoinFragmentBinding> {

    private final ObservableField<String> query = new ObservableField<>();
    public static final String TAG = "ManageCoinFragment";
    private CoinAdapter mCoinAdapter;
    private CoinListViewModel mViewModel;

    private boolean hideConfirmAction = true;

    private boolean isInSearch;

    @Override
    protected int setView() {
        return R.layout.manage_coin_fragment;
    }

    @Override
    protected void init(View view) {

        mActivity.setSupportActionBar(mBinding.toolbar);

        Bundle data = getArguments();
        if (data != null && data.getBoolean(IS_SET_PASSPHRASE)) {
            mBinding.toolbarTitle.setText(R.string.add_coins);
            mBinding.toolbar.setNavigationIcon(new ColorDrawable(Color.TRANSPARENT));
            mBinding.toolbar.setNavigationOnClickListener(null);
            hideConfirmAction = false;
        } else if (mActivity instanceof SetupVaultActivity) {
            mBinding.toolbarTitle.setText(R.string.add_coins);
            mBinding.toolbar.setNavigationIcon(new ColorDrawable(Color.TRANSPARENT));
            mBinding.toolbar.setNavigationOnClickListener(null);
            hideConfirmAction = false;
        } else {
            mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        }

        mBinding.toolbar.setTitle("");
        mCoinAdapter = new CoinAdapter(mActivity, mCoinClickCallback, true);
        mBinding.assetList.setAdapter(mCoinAdapter);
        initSearchView();
    }

    private void initSearchView() {
        mBinding.btnCancel.setOnClickListener(v -> exitSearch());
        View.OnKeyListener backListener = (view, keyCode, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
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
                mCoinAdapter.getFilter().filter(query.get());
            }
        });
        mCoinAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (isInSearch) {
                    if (mCoinAdapter.getItems().size() == 0) {
                        mBinding.empty.setVisibility(View.VISIBLE);
                        mBinding.assetList.setVisibility(View.GONE);
                    } else {
                        mBinding.empty.setVisibility(View.GONE);
                        mBinding.assetList.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.manage, menu);
        if (hideConfirmAction) {
            menu.findItem(R.id.action_confirm).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                Bundle data = Objects.requireNonNull(getArguments());
                if (data.getBoolean(IS_SETUP_VAULT)) {
                    if (FeatureFlags.ENABLE_WHITE_LIST) {
                        navigate(R.id.action_manageCoin_to_manageWhiteList, data);
                    } else {
                        navigate(R.id.action_manageCoinFragment_to_setupSyncFragment, data);
                    }

                } else {
                    startActivity(new Intent(mActivity, MainActivity.class));
                }
                break;
            case R.id.action_search:
                enterSearch();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mViewModel = ViewModelProviders.of(this).get(CoinListViewModel.class);
        if (mActivity instanceof SetupVaultActivity) {
            subscribeUi(MainApplication.getApplication().getRepository().reloadCoins());
        } else if (getArguments() != null && getArguments().getBoolean(IS_SET_PASSPHRASE)) {
            subscribeUi(MainApplication.getApplication().getRepository().reloadCoins());
        } else {
            subscribeUi(mViewModel.getCoins());
        }
    }

    private void subscribeUi(LiveData<List<CoinEntity>> coins) {
        coins.observe(this, coinEntities -> {
            if (coinEntities != null) {
                coinEntities.sort(coinEntityComparator);
                mCoinAdapter.setItems(coinEntities);
            }
            mBinding.executePendingBindings();
        });
    }

    private final CoinClickCallback mCoinClickCallback = coin ->
            AppExecutors.getInstance().diskIO()
                    .execute(() -> mViewModel.toggleCoin(coin));

    private void enterSearch() {
        isInSearch = true;
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
        mBinding.empty.setVisibility(View.GONE);
        mBinding.assetList.setVisibility(View.VISIBLE);
        InputMethodManager inputManager =
                (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(mBinding.search.getWindowToken(), 0);
        }
    }
}
