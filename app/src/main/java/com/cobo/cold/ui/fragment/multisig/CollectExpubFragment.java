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
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.coinlib.ExtendPubkeyFormat;
import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.databinding.CollectExpubBinding;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.XpubFileItemBinding;
import com.cobo.cold.databinding.XpubInputBinding;
import com.cobo.cold.databinding.XpubListBinding;
import com.cobo.cold.ui.common.BaseBindingAdapter;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.FileUtils;
import com.cobo.cold.viewmodel.CollectXpubViewModel;
import com.cobo.cold.viewmodel.SharedDataViewModel;
import com.cobo.cold.viewmodel.XfpNotMatchException;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.cobo.cold.viewmodel.GlobalViewModel.hasSdcard;
import static com.cobo.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.cobo.cold.viewmodel.MultiSigViewModel.convertXpub;

public class CollectExpubFragment extends MultiSigBaseFragment<CollectExpubBinding>
        implements CollectXpubClickHandler {

    private Adapter adapter;
    private List<CollectXpubViewModel.XpubInfo> data;
    private int total;
    private int threshold;
    private MultiSig.Account account;
    private String path;
    private CollectXpubViewModel collectXpubViewModel;

    @Override
    protected int setView() {
        return R.layout.collect_expub;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        extractArguments();
        initializeData();
        mBinding.walletType.setText(getString(R.string.wallet_type, threshold + "-" + total));
        mBinding.addressType.setText(getString(R.string.address_type, getAddressTypeString(account)));
        mBinding.hint.setOnClickListener( v -> showCommonModal(mActivity, getString(R.string.export_multisig_xpub),
                getString(R.string.invalid_xpub_file_hint), getString(R.string.know),null));
        adapter = new Adapter();
        mBinding.list.setAdapter(adapter);
        mBinding.create.setOnClickListener(v -> createWallet());
        mBinding.create.setEnabled(data.stream()
                .allMatch(i -> !TextUtils.isEmpty(i.xpub) && !TextUtils.isEmpty(i.xfp)));
        if (!collectXpubViewModel.startCollect) {
            showHint();
            collectXpubViewModel.startCollect = true;
        }
    }

    private void showHint() {
        ModalDialog.showCommonModal(mActivity,getString(R.string.check_input_pub_key),
                getString(R.string.check_pub_key_hint),
                getString(R.string.know),null);
    }

    private void createWallet() {
        try {
            JSONArray array = new JSONArray();
            for (CollectXpubViewModel.XpubInfo info : data) {
                JSONObject xpub = new JSONObject();
                if (ExtendPubkeyFormat.isValidXpub(info.xpub)) {
                    xpub.put("xfp", info.xfp);
                    xpub.put("xpub", convertXpub(info.xpub, MultiSig.Account.ofPath(path)));
                    array.put(xpub);
                }
            }
            viewModel.createMultisigWallet(threshold, account, null, array)
                    .observe(this, walletEntity -> {
                        if (walletEntity != null) {
                            Bundle data = new Bundle();
                            data.putString("wallet_fingerprint", walletEntity.getWalletFingerPrint());
                            data.putBoolean("setup", true);
                            navigate(R.id.action_export_wallet_to_cosigner, data);
                        }
                    });
        } catch (JSONException | XfpNotMatchException e) {
            e.printStackTrace();
        }
    }

    private void extractArguments() {
        Bundle bundle = getArguments();
        Objects.requireNonNull(bundle);
        total = bundle.getInt("total");
        threshold = bundle.getInt("threshold");
        path = bundle.getString("path");
        account = MultiSig.Account.ofPath(path);
    }

    private String getAddressTypeString(MultiSig.Account account) {
        int id = R.string.multi_sig_account_segwit;

        if (account == MultiSig.Account.P2WSH_P2SH) {
            id = R.string.multi_sig_account_p2sh;
        } else if (account == MultiSig.Account.P2SH) {
            id = R.string.multi_sig_account_legacy;
        }

        return getString(id);
    }

    private void initializeData() {
        collectXpubViewModel = ViewModelProviders.of(mActivity).get(CollectXpubViewModel.class);
        data = collectXpubViewModel.getXpubInfo();
    }

    @Override
    public void onClickDelete(CollectXpubViewModel.XpubInfo info) {
        info.xpub = null;
        info.xfp = null;
        adapter.notifyItemChanged(info.index - 1);
        mBinding.create.setEnabled(data.stream()
                .allMatch(i -> !TextUtils.isEmpty(i.xpub) && !TextUtils.isEmpty(i.xfp)));
    }

    @Override
    public void onClickScan(CollectXpubViewModel.XpubInfo info) {
        SharedDataViewModel viewModel =
                ViewModelProviders.of(mActivity).get(SharedDataViewModel.class);
        MutableLiveData<String> scanResult = viewModel.getScanResult();
        scanResult.observe(mActivity, s -> {
            if (!TextUtils.isEmpty(s)) {
                try {
                    JSONObject object = new JSONObject(s);
                    String xfp = object.getString("xfp");
                    String xpub = object.getString("xpub");
                    String path = object.getString("path");
                    if (path.equals(CollectExpubFragment.this.path)) {
                        updateXpubInfo(info, xfp, xpub);
                    } else {
                        showCommonModal(mActivity, getString(R.string.wrong_xpub_format),
                                getString(R.string.wrong_xpub_format_hint, getAddressTypeString(account),
                                        getAddressTypeString(MultiSig.Account.ofPrefix(xpub.substring(0,4)))),
                                getString(R.string.know),null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        showCommonModal(mActivity,getString(R.string.invalid_xpub_file),
                                getString(R.string.invalid_xpub_file_hint),
                                getString(R.string.know),null);
                    } catch (Exception ignore){}
                } finally {
                    scanResult.setValue("");
                    scanResult.removeObservers(mActivity);
                }
            }
        });
        Bundle data = new Bundle();
        data.putString("purpose", "collect_xpub");
        navigate(R.id.scan_xpub_info, data);
    }

    private void updateXpubInfo(CollectXpubViewModel.XpubInfo info, String xfp, String xpub) {
        for (CollectXpubViewModel.XpubInfo xpubInfo : data) {
            if (xpub.equals(xpubInfo.xpub)) {
                ModalDialog.showCommonModal(mActivity, getString(R.string.duplicate_xpub_title),
                        getString(R.string.duplicate_xpub_hint),
                        getString(R.string.know), null);
                return;
            }
        }
        info.xpub = xpub;
        info.xfp = xfp;
        data.set(info.index - 1, info);
        adapter.notifyItemChanged(info.index - 1);
        mBinding.create.setEnabled(data.stream()
                .allMatch(i -> !TextUtils.isEmpty(i.xpub) && !TextUtils.isEmpty(i.xfp)));
    }

    @Override
    public void onClickSdcard(CollectXpubViewModel.XpubInfo info) {
        if (!hasSdcard(mActivity)) {
            showXpubList(new ArrayList<>(),info);
        } else {
            collectXpubViewModel.loadXpubFile().observe(this, files -> showXpubList(files, info));
        }
    }

    private void showXpubList(List<File> files, CollectXpubViewModel.XpubInfo info) {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        XpubListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.xpub_list, null, false);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        if (!files.isEmpty()) {
            FileListAdapter adapter = new FileListAdapter(mActivity, dialog, info);
            adapter.setItems(files);
            binding.list.setAdapter(adapter);
            binding.list.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        } else {
            binding.list.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
            if (!hasSdcard(mActivity)) {
                binding.emptyTitle.setText(R.string.no_sdcard);
                binding.emptyMessage.setText(R.string.no_sdcard_hint);
            } else {
                binding.emptyTitle.setText(R.string.no_pub_file_found);
                binding.emptyMessage.setText(R.string.no_pub_file_found);
            }
        }
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void decodeXpubFile(File file, CollectXpubViewModel.XpubInfo info) {
        try {
            JSONObject obj = new JSONObject(FileUtils.readString(file));

            String xpub;
            if (obj.has("xpub")) {
                xpub = obj.getString("xpub");
            } else {
                xpub = obj.getString(account.getFormat().toLowerCase().replace("-", "_"));
            }
            if (!xpub.startsWith(account.getXpubPrefix())) {
                ModalDialog.showCommonModal(mActivity,getString(R.string.wrong_xpub_format),
                        getString(R.string.wrong_xpub_format_hint,getAddressTypeString(account),
                                getAddressTypeString(MultiSig.Account.ofPrefix(xpub.substring(0,4)))),
                        getString(R.string.know),null);
                return;
            }
            updateXpubInfo(info, obj.getString("xfp"), xpub);
        } catch (JSONException e) {
            e.printStackTrace();
            showCommonModal(mActivity,getString(R.string.invalid_xpub_file),
                    getString(R.string.invalid_xpub_file_hint),
                    getString(R.string.know),null);
        }
    }

    private String format(CollectXpubViewModel.XpubInfo info) {
        String index = info.index < 10 ? "0" + info.index : String.valueOf(info.index);
        if (TextUtils.isEmpty(info.xpub)) {
            return index + " ";
        } else {
            return index + " Fingerprint:" + info.xfp + "\n"
                    + mActivity.getString(R.string.extend_pubkey1) + ":" + info.xpub;
        }
    }

    class FileListAdapter extends BaseBindingAdapter<File, XpubFileItemBinding> {
        int selectIndex = -1;
        BottomSheetDialog dialog;
        CollectXpubViewModel.XpubInfo info;
        FileListAdapter(Context context, BottomSheetDialog dialog, CollectXpubViewModel.XpubInfo info) {
            super(context);
            this.dialog = dialog;
            this.info = info;
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.xpub_file_item;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            XpubFileItemBinding binding = DataBindingUtil.getBinding(holder.itemView);
            if (binding != null) {
                if (position == selectIndex) {
                    binding.icon.setVisibility(View.VISIBLE);
                } else {
                    binding.icon.setVisibility(View.GONE);
                }
                onBindItem(binding, this.items.get(position));

                binding.getRoot().setOnClickListener(v -> {
                    selectIndex = position;
                    notifyDataSetChanged();
                    dialog.dismiss();
                    if (selectIndex != -1) {
                        decodeXpubFile(getItems().get(selectIndex), info);
                    }
                });
            }
        }

        @Override
        protected void onBindItem(XpubFileItemBinding binding, File item) {
            binding.text.setText(item.getName());
        }

    }

    class Adapter extends RecyclerView.Adapter<VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.xpub_input, parent, false).getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            XpubInputBinding binding = DataBindingUtil.getBinding(holder.itemView);
            Objects.requireNonNull(binding).setData(data.get(position));
            binding.setClickHandler(CollectExpubFragment.this);
            binding.text.setText(format(data.get(position)));
            binding.executePendingBindings();
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    class VH extends RecyclerView.ViewHolder {
        VH(@NonNull View itemView) {
            super(itemView);
        }
    }


    private static ModalDialog showCommonModal(AppCompatActivity activity,
                                               String title,
                                               String subTitle,
                                               String buttonText,
                                               Runnable confirmAction) {
        ModalDialog dialog = new ModalDialog();
        CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(activity),
                R.layout.common_modal, null, false);
        binding.title.setText(title);
        binding.subTitle.setText(subTitle);
        binding.subTitle.setGravity(Gravity.LEFT);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(buttonText);
        binding.confirm.setOnClickListener(v -> {
            if (confirmAction != null) {
                confirmAction.run();
            }
            dialog.dismiss();
        });
        dialog.setBinding(binding);
        dialog.show(activity.getSupportFragmentManager(), "");
        return dialog;
    }

}


