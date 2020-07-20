package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.R;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.databinding.ExportSuccessBinding;
import com.cobo.cold.databinding.ImportWalletBinding;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.viewmodel.XfpNotMatchException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.cobo.cold.ui.fragment.multisig.MultisigWalletInfoFragment.getXpub;

public class ImportWalletFragment extends MultiSigBaseFragment<ImportWalletBinding> {

    private JSONObject walletInfo;
    private MultiSig.Account account;
    private int threshold;
    private String creator;
    private MultiSigWalletEntity dummyWallet;

    @Override
    protected int setView() {
        return R.layout.import_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        dummyWallet = constructWalletEntity(data);
        mBinding.setWallet(dummyWallet);
        mBinding.setAddressType(MultiSig.Account.ofPath(dummyWallet.getExPubPath()).getFormat());
        mBinding.setXpubInfo(getXpub(dummyWallet));
        mBinding.confirm.setOnClickListener(v -> showVerifyCode());
        mBinding.cancel.setOnClickListener(v -> navigateUp());
        showCheckDialog();

    }

    private void showCheckDialog() {
        ModalDialog.showCommonModal(mActivity,"请检查钱包信息",
                "导入前，请对钱包信息进行检查，确保和已创建的钱包信息保持一致后再导入。",
                getString(R.string.know), null);
    }

    private void showVerifyCode() {
        if ("CoboVault".equals(creator)) {
            try {
                List<String> xpubs = new ArrayList<>();
                JSONArray array = new JSONArray(dummyWallet.getExPubs());
                for (int i = 0; i < array.length(); i++) {
                    xpubs.add(array.getJSONObject(i).getString("xpub"));
                }
                ModalDialog dialog = new ModalDialog();
                CommonModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                        R.layout.common_modal, null, false);
                binding.title.setText("钱包校验");
                binding.subTitle.setText(getString(R.string.verify_wallet_hint,
                        viewModel.calculateWalletFingerptint(threshold, xpubs, account.getPath())));
                binding.close.setVisibility(View.GONE);
                binding.confirm.setText("验证码一致，继续导入");
                binding.confirm.setOnClickListener(v -> {
                    importWallet();
                    dialog.dismiss();
                });
                binding.btn1.setVisibility(View.VISIBLE);
                binding.btn1.setText("验证码不一致，检查钱包信息");
                binding.btn1.setOnClickListener(v -> dialog.dismiss());
                dialog.setBinding(binding);
                dialog.show(mActivity.getSupportFragmentManager(), "");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private MultiSigWalletEntity constructWalletEntity(Bundle data) {
        try {
            walletInfo = new JSONObject(data.getString("wallet_info"));
            threshold = Integer.parseInt(walletInfo.getString("Policy").split(" of ")[0]);
            int total = Integer.parseInt(walletInfo.getString("Policy").split(" of ")[1]);
            account = MultiSig.Account.ofPath(walletInfo.getString("Derivation"));
            creator = walletInfo.optString("Creator");

            return new MultiSigWalletEntity(walletInfo.getString("Name"),
                    threshold, total,account.getPath(),walletInfo.getJSONArray("Xpubs").toString(),"","");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void importWallet() {
        try {
            viewModel.createMultisigWallet(threshold, account, walletInfo.getJSONArray("Xpubs"))
                    .observe(this, this::onImportWalletSuccess);
        } catch (XfpNotMatchException | JSONException e) {
            e.printStackTrace();
        }

    }

    private void onImportWalletSuccess(MultiSigWalletEntity walletEntity) {
        Handler handler = new Handler();
        if (walletEntity != null) {
            ModalDialog dialog = ModalDialog.newInstance();
            ExportSuccessBinding binding =
                    DataBindingUtil.inflate(LayoutInflater.from(mActivity),R.layout.export_success,
                            null,false);
            binding.text.setText(R.string.import_success);
            dialog.setBinding(binding);
            dialog.show(mActivity.getSupportFragmentManager(),"");
            handler.postDelayed(() -> {
                dialog.dismiss();
                Bundle bundle = Bundle.forPair("wallet_fingerprint", walletEntity.getWalletFingerPrint());
                bundle.putBoolean("isImportMultisig",true);
                navigate(R.id.action_export_wallet_to_electrum, bundle);
            },500);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
