package com.cobo.cold.ui.fragment.multisig;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.cold.R;
import com.cobo.cold.databinding.ElectrumTxnBinding;
import com.cobo.cold.databinding.FileListBinding;
import com.cobo.cold.ui.common.BaseBindingAdapter;
import com.cobo.cold.ui.fragment.main.electrum.Callback;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.FileUtils;
import com.cobo.cold.update.utils.Storage;
import com.cobo.cold.util.Keyboard;
import com.cobo.cold.viewmodel.InvalidMultisigWalletException;
import com.cobo.cold.viewmodel.MultiSigViewModel;
import com.cobo.cold.viewmodel.SharedDataViewModel;

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
        try {
            JSONObject walletFile = MultiSigViewModel.decodeColdCardWalletFile(
                    FileUtils.readString(new File(storage.getExternalDir(), file)));
            Bundle data = new Bundle();
            data.putString("wallet_info",walletFile.toString());
            navigate(R.id.import_multisig_wallet, data);
        } catch (InvalidMultisigWalletException e) {
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

    static class Adapter extends BaseBindingAdapter<String, ElectrumTxnBinding> {
        private Callback callback;

        Adapter(Context context, Callback callback) {
            super(context);
            this.callback = callback;
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.electrum_txn;
        }

        @Override
        protected void onBindItem(ElectrumTxnBinding binding, String item) {
            binding.setFile(item);
            binding.setCallback(callback);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
        }
    }
}
