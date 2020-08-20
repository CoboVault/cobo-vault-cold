/*
 *
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
 *
 */

package com.cobo.cold.ui.fragment.multisig;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.FileListBinding;
import com.cobo.cold.databinding.FileListItemBinding;
import com.cobo.cold.ui.common.BaseBindingAdapter;
import com.cobo.cold.ui.fragment.main.electrum.Callback;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.FileUtils;
import com.cobo.cold.update.utils.Storage;
import com.cobo.cold.viewmodel.InvalidMultisigWalletException;
import com.cobo.cold.viewmodel.MultiSigViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cobo.cold.viewmodel.GlobalViewModel.hasSdcard;


public class ImportMultisigFileList extends MultiSigBaseFragment<FileListBinding>
        implements Callback, Toolbar.OnMenuItemClickListener {
    private Adapter adapter;
    private AtomicBoolean showEmpty;
    @Override
    protected int setView() {
        return R.layout.file_list;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbar.inflateMenu(R.menu.main);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        mBinding.toolbarTitle.setText(R.string.import_multisig_wallet);
        adapter = new Adapter(mActivity, this);
        initViews();
    }

    private void initViews() {
        showEmpty = new AtomicBoolean(false);
        if (!hasSdcard(mActivity)) {
            showEmpty.set(true);
            mBinding.emptyTitle.setText(R.string.no_sdcard);
            mBinding.emptyMessage.setText(R.string.no_sdcard_hint);
        } else {
            mBinding.list.setAdapter(adapter);
            viewModel.loadWalletFile().observe(this, files -> {
                if (files.size() > 0) {
                    adapter.setItems(files);
                } else {
                    showEmpty.set(true);
                    mBinding.emptyTitle.setText(R.string.no_multisig_wallet_file);
                    mBinding.emptyMessage.setText(R.string.no_multisig_wallet_file_hint);
                }
                updateUi();
            });
        }
        updateUi();
    }

    private void updateUi() {
        if (showEmpty.get()) {
            mBinding.emptyView.setVisibility(View.VISIBLE);
            mBinding.list.setVisibility(View.GONE);
        } else {
            mBinding.emptyView.setVisibility(View.GONE);
            mBinding.list.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(String file) {
        Storage storage = Storage.createByEnvironment(mActivity);
        Objects.requireNonNull(storage);
        try {
            JSONObject walletFile = MultiSigViewModel.decodeColdCardWalletFile(
                    FileUtils.readString(new File(storage.getExternalDir(), file)));
            String path = walletFile.getString("Derivation");
            boolean isTestnet = !Utilities.isMainNet(mActivity);
            if (MultiSig.Account.ofPath(path).isTest() != isTestnet) {
                String currentNet = isTestnet ? getString(R.string.testnet) : getString(R.string.mainnet);
                String walletFileNet = MultiSig.Account.ofPath(path).isTest() ? getString(R.string.testnet) : getString(R.string.mainnet);
                ModalDialog.showCommonModal(mActivity, getString(R.string.import_failed),
                        getString(R.string.import_failed_network_not_match, currentNet, walletFileNet, walletFileNet),
                        getString(R.string.know),null);
                return;
            }

            Bundle data = new Bundle();
            data.putString("wallet_info",walletFile.toString());
            navigate(R.id.import_multisig_wallet, data);
        } catch (InvalidMultisigWalletException | JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            Bundle data = new Bundle();
            data.putString("purpose", "importMultiSigWallet");
            navigate(R.id.action_scan_multisig_wallet, data);
        }
        return true;
    }

    static class Adapter extends BaseBindingAdapter<String, FileListItemBinding> {
        private Callback callback;

        Adapter(Context context, Callback callback) {
            super(context);
            this.callback = callback;
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.file_list_item;
        }

        @Override
        protected void onBindItem(FileListItemBinding binding, String item) {
            binding.setFile(item);
            binding.setCallback(callback);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
        }
    }
}
