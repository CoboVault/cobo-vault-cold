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
import android.view.View;

import androidx.annotation.Nullable;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.AssetItemBinding;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.common.FilterableBaseBindingAdapter;
import com.cobo.cold.viewmodel.WatchWallet;

public class CoinAdapter extends FilterableBaseBindingAdapter<CoinEntity, AssetItemBinding> {

    private final CoinClickCallback mCoinClickCallback;
    private final boolean isManageCoin;

    public CoinAdapter(Context context, @Nullable CoinClickCallback clickCallback, boolean isManageCoin) {
        super(context);
        mCoinClickCallback = clickCallback;
        this.isManageCoin = isManageCoin;
    }

    @Override
    protected int getLayoutResId(int viewType) {
        return R.layout.asset_item;
    }

    @Override
    protected void onBindItem(AssetItemBinding binding, CoinEntity item) {
        if (WatchWallet.getWatchWallet(context) != WatchWallet.POLKADOT_JS) {
            binding.setIsManage(isManageCoin);
        }
        binding.setCallback(mCoinClickCallback);
        binding.setCoin(item);
        if (isManageCoin || Coins.showPublicKey(item.getCoinCode())) {
            binding.addrNum.setVisibility(View.GONE);
            binding.addr.setVisibility(View.GONE);
        }
    }

}
