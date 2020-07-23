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
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.cold.R;
import com.cobo.cold.databinding.FileListBinding;
import com.cobo.cold.databinding.FileListItemBinding;
import com.cobo.cold.ui.common.BaseBindingAdapter;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.fragment.main.electrum.Callback;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.FileUtils;
import com.cobo.cold.update.utils.Storage;
import com.cobo.cold.viewmodel.PsbtViewModel;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cobo.cold.viewmodel.GlobalViewModel.hasSdcard;

public class PsbtListFragment extends BaseFragment<FileListBinding>
        implements Callback {

    public static final String TAG = "PsbtListFragment";
    public static final String PSBT_MAGIC_PREFIX = Hex.toHexString("psbt".getBytes(StandardCharsets.UTF_8));
    private PsbtViewModel viewModel;
    private FileListAdapter adapter;
    private AtomicBoolean showEmpty;
    private boolean multisig;

    @Override
    protected int setView() {
        return R.layout.file_list;
    }

    @Override
    protected void init(View view) {

        if (getArguments() != null) {
            multisig = getArguments().getBoolean("multisig");
        }
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        viewModel = ViewModelProviders.of(mActivity).get(PsbtViewModel.class);
        adapter = new FileListAdapter(mActivity, this);
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
            viewModel.loadUnsignPsbt().observe(this, files -> {
                if (files.size() > 0) {
                    adapter.setItems(files);
                } else {
                    showEmpty.set(true);
                    mBinding.emptyTitle.setText(R.string.no_unsigned_txn);
                    mBinding.emptyMessage.setText(R.string.no_unsigned_txn_hint);
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
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onClick(String file) {
        Storage storage = Storage.createByEnvironment(mActivity);
        byte[] content = FileUtils.bufferlize(new File(storage.getExternalDir(), file));
        if (content != null) {
            String psbtBase64 = null;
            if (isBase64Psbt(content)) {
                psbtBase64 = new String(content);
            } else if (Hex.toHexString(content).startsWith(PSBT_MAGIC_PREFIX)) {
                psbtBase64 = Base64.toBase64String(content);
            }

            if (psbtBase64 != null) {
                Bundle bundle = new Bundle();
                bundle.putString("psbt_base64", psbtBase64);
                bundle.putBoolean("multisig",multisig);
                navigate(R.id.action_to_psbtTxConfirmFragment, bundle);
                return;
            }
        }

        ModalDialog.showCommonModal(mActivity,
                getString(R.string.electrum_decode_txn_fail),
                getString(R.string.error_txn_file),
                getString(R.string.confirm),
                null);

    }

    private boolean isBase64Psbt(byte[] content) {
        try {
            byte[] data = Base64.decode(new String(content));
            return Hex.toHexString(data).startsWith(PSBT_MAGIC_PREFIX);
        }catch (Exception e) {
            return false;
        }
    }


    public static class FileListAdapter extends BaseBindingAdapter<String, FileListItemBinding> {
        private Callback callback;

        FileListAdapter(Context context, Callback callback) {
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
