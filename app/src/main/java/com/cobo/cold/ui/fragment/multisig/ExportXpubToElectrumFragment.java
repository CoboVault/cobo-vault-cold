package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.ExportSdcardModalBinding;
import com.cobo.cold.databinding.ExportXpubToElectrumBinding;
import com.cobo.cold.databinding.ModalWithTwoButtonBinding;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import static com.cobo.cold.viewmodel.GlobalViewModel.exportSuccess;
import static com.cobo.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.cobo.cold.viewmodel.GlobalViewModel.writeToSdcard;

public class ExportXpubToElectrumFragment extends MultiSigBaseFragment<ExportXpubToElectrumBinding> {

    private ArrayList<XpubInfo> xpubs;
    private int index = 0;
    private MultiSigWalletEntity walletEntity;

    @Override
    protected int setView() {
        return R.layout.export_xpub_to_electrum;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        mBinding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
        viewModel.getWalletEntity(data.getString("wallet_fingerprint"))
                .observe(this, walletEntity -> {
                    this.walletEntity = walletEntity;
                    try {
                        JSONArray array = new JSONArray(walletEntity.getExPubs());
                        xpubs = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {

                            xpubs.add(new XpubInfo(array.getJSONObject(i).getString("xfp"),
                                    array.getJSONObject(i).getString("xpub")));
                        }

                        updateUI();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

        mBinding.next.setOnClickListener(v -> {
            if (index < xpubs.size() - 1) {
                index++;
                updateUI();
            } else {
                popBackStack(R.id.multisigFragment, false);
            }
        });

        mBinding.prev.setOnClickListener(v -> {
            if (index > 0) {
                index--;
            }
            updateUI();
        });

        mBinding.exportToSdcard.setOnClickListener(v -> exportXpub());
        mBinding.info.setOnClickListener(v -> showElectrumInfo());
    }

    private void onBackPressed() {
        if (index < xpubs.size() - 1) {
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.modal_with_two_button, null, false);
            binding.title.setText(R.string.stop_export_xpub);
            binding.subTitle.setText(getString(R.string.stop_export_xpub_hint, xpubs.size() - 1 - index));
            binding.actionHint.setText("");
            binding.left.setText(R.string.create_later);
            binding.left.setOnClickListener(left -> {
                dialog.dismiss();
                popBackStack(R.id.multisigFragment, false);
            });
            binding.right.setText(R.string.keep_create);
            binding.right.setOnClickListener(right -> dialog.dismiss());
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");
        } else {
            navigateUp();
        }
    }

    private void showElectrumInfo() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.electrum_import_xpub_guide_title);
        binding.subTitle.setText(getString(R.string.export_multisig_wallet_to_electrum_guide,
                walletEntity.getTotal(), walletEntity.getThreshold()));
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }

    private void exportXpub() {
        Storage storage = Storage.createByEnvironment(mActivity);
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            ModalDialog modalDialog = ModalDialog.newInstance();
            ExportSdcardModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.export_sdcard_modal, null, false);
            binding.title.setText(R.string.export_xpub_text_file);
            binding.fileName.setText(getFileName());
            binding.cancel.setOnClickListener(vv -> modalDialog.dismiss());
            binding.confirm.setOnClickListener(vv -> {
                modalDialog.dismiss();
                if (writeToSdcard(storage, xpubs.get(index).xpub, getFileName())) {
                    exportSuccess(mActivity, null);
                }
            });
            modalDialog.setBinding(binding);
            modalDialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }

    private String getFileName() {
        MultiSig.Account account = MultiSig.Account.ofPath(walletEntity.getExPubPath());
        return xpubs.get(index).xfp + "-" + account.getFormat() + ".txt";
    }

    private void updateUI() {
        mBinding.index.setText((index + 1) + "/" + xpubs.size());
        mBinding.qrcode.setData(xpubs.get(index).xpub);
        mBinding.expub.setText(xpubs.get(index).xpub);
        mBinding.prev.setVisibility(index > 0 ? View.VISIBLE : View.INVISIBLE);
        mBinding.next.setText(index < xpubs.size() - 1 ? R.string.next_one : R.string.complete);
        mBinding.totalKeyNumber.setText(getString(R.string.total_key_number, xpubs.size()));
    }

    class XpubInfo {
        String xfp;
        String xpub;

        XpubInfo(String xfp, String xpub) {
            this.xfp = xfp;
            this.xpub = xpub;
        }
    }
}
