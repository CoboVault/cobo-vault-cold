package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.databinding.ExportMultisigExpubBinding;
import com.cobo.cold.databinding.ModalWithTwoButtonBinding;
import com.cobo.cold.databinding.SwitchXpubBottomSheetBinding;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.Storage;
import com.cobo.cold.viewmodel.MultiSigViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import static com.cobo.cold.viewmodel.GlobalViewModel.exportSuccess;
import static com.cobo.cold.viewmodel.GlobalViewModel.showNoSdcardModal;
import static com.cobo.cold.viewmodel.GlobalViewModel.writeToSdcard;

public class ExportMultisigExpubFragment extends BaseFragment<ExportMultisigExpubBinding>
        implements Toolbar.OnMenuItemClickListener {
    public static final String TAG = "ExportMultisigExpubFragment";

    private MultiSig.Account account = MultiSig.Account.P2WSH;
    private MultiSigViewModel viewModel;

    @Override
    protected int setView() {
        return R.layout.export_multisig_expub;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.toolbar.inflateMenu(R.menu.export_all);
        mBinding.toolbar.setOnMenuItemClickListener(this);
        viewModel = ViewModelProviders.of(mActivity).get(MultiSigViewModel.class);
        updateUI();
        mBinding.addressType.setOnClickListener(v -> showBottomSheetMenu());
        mBinding.exportToSdcard.setOnClickListener(v -> exportToSdcard());
    }

    private void exportToSdcard() {
        Storage storage = Storage.createByEnvironment(mActivity);
        if (storage == null || storage.getExternalDir() == null) {
            showNoSdcardModal(mActivity);
        } else {
            ModalDialog dialog = new ModalDialog();
            ModalWithTwoButtonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                    R.layout.modal_with_two_button,
                    null, false);
            binding.title.setText(R.string.keep_rolling);
            binding.subTitle.setText("导出扩展公钥");
            binding.left.setText(R.string.cancel);
            binding.left.setOnClickListener(v -> {
                dialog.dismiss();
            });
            binding.right.setText(R.string.export);
            binding.right.setOnClickListener(v -> {
                dialog.dismiss();
                if (writeToSdcard(storage, viewModel.getExportXpubInfo(account),
                        viewModel.getExportXpubFileName(account))) {
                    Runnable runnable;
                    if (mActivity instanceof SetupVaultActivity) {
                        runnable = () -> navigate(R.id.action_to_setupCompleteFragment);
                    } else {
                        runnable = () -> popBackStack(R.id.assetFragment,false);
                    }
                    exportSuccess(mActivity, runnable);
                }
            });
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(), "");
        }
    }

    private void updateUI() {
        int accountType = R.string.multi_sig_account_segwit;
        switch (account) {
            case P2SH_P2WSH:
                accountType = R.string.multi_sig_account_p2sh;
                break;
            case P2SH:
                accountType = R.string.multi_sig_account_legacy;
                break;
            case P2WSH:
                break;
        }
        String xpub = viewModel.getXpub(account);
        mBinding.addressType.setText(String.format("%s >", getString(accountType)));
        mBinding.expub.setText(xpub);
        mBinding.path.setText(String.format("(%s)", account.getPath()));
        mBinding.qrcode.setData(viewModel.getExportXpubInfo(account));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export_all) {
            ModalDialog dialog = ModalDialog.newInstance();
        }
        return true;
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        SwitchXpubBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.switch_xpub_bottom_sheet, null, false);
        refreshCheckedStatus(binding.getRoot());
        binding.nativeSegwit.setOnClickListener(v -> onXpubSwitch(dialog, MultiSig.Account.P2WSH));
        binding.nestedSegeit.setOnClickListener(v -> onXpubSwitch(dialog, MultiSig.Account.P2SH_P2WSH));
        binding.legacy.setOnClickListener(v -> onXpubSwitch(dialog, MultiSig.Account.P2SH));
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    private void onXpubSwitch(BottomSheetDialog dialog,
                              MultiSig.Account account) {
        this.account = account;
        dialog.dismiss();
        updateUI();
    }

    private void refreshCheckedStatus(View view) {
        for (MultiSig.Account value : MultiSig.Account.values()) {
            view.findViewWithTag(value.getFormat()).setVisibility(View.GONE);
        }
        view.findViewWithTag(account.getFormat()).setVisibility(View.VISIBLE);
    }
}
