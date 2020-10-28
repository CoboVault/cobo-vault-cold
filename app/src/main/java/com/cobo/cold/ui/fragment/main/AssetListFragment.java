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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.R;
import com.cobo.cold.databinding.AssetListFragmentBinding;
import com.cobo.cold.db.PresetData;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.CoinListViewModel;
import com.cobo.cold.viewmodel.SetupVaultViewModel;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.util.List;
import java.util.stream.Collectors;

import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_CODE;
import static com.cobo.cold.ui.fragment.Constants.KEY_COIN_ID;
import static com.cobo.cold.ui.fragment.Constants.KEY_ID;
import static com.cobo.cold.viewmodel.CoinListViewModel.coinEntityComparator;

public class AssetListFragment extends BaseFragment<AssetListFragmentBinding> {

    public static final String TAG = "AssetListFragment";

    private long startTime;
    public static final int REQUEST_CODE_SCAN = 1000;
    private CoinAdapter mCoinAdapter;


    @Override
    protected int setView() {
        return R.layout.asset_list_fragment;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        mBinding.toolbar.setTitle("");
        mCoinAdapter = new CoinAdapter(mActivity, mCoinClickCallback, false);
        mBinding.assetList.setAdapter(mCoinAdapter);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        CoinListViewModel mViewModel = ViewModelProviders.of(mActivity).get(CoinListViewModel.class);
        subscribeUi(mViewModel.getCoins());
        checkAndAddNewCoins();
    }

    private void checkAndAddNewCoins() {
        SetupVaultViewModel viewModel = ViewModelProviders.of(mActivity)
                .get(SetupVaultViewModel.class);
        AppExecutors.getInstance().diskIO().execute(()
                -> viewModel.presetData(PresetData.generateCoins(mActivity), null)
        );

    }

    private void subscribeUi(LiveData<List<CoinEntity>> coins) {
        coins.observe(this, coinEntities -> {
            if (coinEntities != null) {
                List<CoinEntity> toShow = coinEntities
                        .stream()
                        .filter(CoinEntity::isShow)
                        .filter(coinEntity -> Coins.isCoinSupported(coinEntity.getCoinCode()))
                        .collect(Collectors.toList());
                if (toShow.isEmpty()) {
                    mBinding.setIsEmpty(true);
                } else {
                    mBinding.setIsEmpty(false);
                    toShow.sort(coinEntityComparator);
                    mCoinAdapter.setItems(toShow);
                }

            } else {
                mBinding.setIsEmpty(true);
            }
            mBinding.executePendingBindings();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            AndPermission.with(this)
                    .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                    .onGranted(permissions -> {
                        startTime = System.currentTimeMillis();
                        Bundle bundle = new Bundle();
                        bundle.putLong("starttime", startTime);
                        navigate(R.id.action_to_scan, bundle);
                    })
                    .onDenied(permissions -> {
                        Uri packageURI = Uri.parse("package:" + mActivity.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        Toast.makeText(mActivity, getString(R.string.scan_permission_denied), Toast.LENGTH_LONG).show();
                    }).start();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private final CoinClickCallback mCoinClickCallback = coin -> {
        Bundle data = new Bundle();
        data.putLong(KEY_ID, coin.getId());
        data.putString(KEY_COIN_ID, coin.getCoinId());
        data.putString(KEY_COIN_CODE, coin.getCoinCode());
        navigate(R.id.action_to_assetFragment, data);
    };
}
