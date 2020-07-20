package com.cobo.cold.ui.fragment.multisig;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.cobo.coinlib.ExtendPubkeyFormat;
import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.databinding.CollectExpubBinding;
import com.cobo.cold.databinding.ModalWithTwoButtonBinding;
import com.cobo.cold.databinding.XpubFileItemBinding;
import com.cobo.cold.databinding.XpubInputBinding;
import com.cobo.cold.databinding.XpubListBinding;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
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
import java.util.List;
import java.util.Objects;

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
        adapter = new Adapter();
        mBinding.list.setAdapter(adapter);
        mBinding.create.setOnClickListener(v -> createWallet());
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
            viewModel.createMultisigWallet(threshold, account, array)
                    .observe(this, walletEntity -> {
                        if (walletEntity != null) {
                            Bundle data = new Bundle();
                            data.putString("wallet_fingerprint",walletEntity.getWalletFingerPrint());
                            navigate(R.id.action_export_wallet_to_cosigner);
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
                        scanResult.setValue("");
                        scanResult.removeObservers(mActivity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
                ModalDialog.showCommonModal(mActivity, "重复的扩展公钥",
                        "该扩展公钥您已填入，请填入其他参与者的扩展公钥",
                        getString(R.string.know), null);
                return;
            }
        }
        info.xpub = xpub;
        info.xfp = xfp;
        data.set(info.index - 1, info);
        adapter.notifyItemChanged(info.index - 1);
    }

    @Override
    public void onClickSdcard(CollectXpubViewModel.XpubInfo info) {
        collectXpubViewModel.loadXpubFile().observe(this, files -> showXpubList(files, info));
    }

    private void showXpubList(List<File> files, CollectXpubViewModel.XpubInfo info) {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        XpubListBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.xpub_list, null, false);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        FileListAdapter adapter = new FileListAdapter(mActivity);
        adapter.setItems(files);
        binding.list.setAdapter(adapter);
        binding.confirm.setOnClickListener(v -> {
            if (adapter.selectIndex != -1) {
                decodeXpubFile(files.get(adapter.selectIndex), info);
            }
            dialog.dismiss();
        });
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
            updateXpubInfo(info, obj.getString("xfp"), xpub);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String format(CollectXpubViewModel.XpubInfo info) {
        String index = info.index < 10 ? "0" + info.index : String.valueOf(info.index);
        if (TextUtils.isEmpty(info.xpub)) {
            return index + ":";
        } else {
            return index + ":Fingerprint:" + info.xfp + "\n"
                    + mActivity.getString(R.string.extend_pubkey) + ":" + info.xpub;
        }
    }

    class FileListAdapter extends BaseBindingAdapter<File, XpubFileItemBinding> {
        int selectIndex = -1;

        FileListAdapter(Context context) {
            super(context);
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


}


