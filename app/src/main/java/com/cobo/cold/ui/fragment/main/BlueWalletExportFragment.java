package com.cobo.cold.ui.fragment.main;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.cobo.cold.R;
import com.cobo.cold.databinding.BlueExportBinding;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.SetupVaultActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.viewmodel.GlobalViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cobo.coinlib.ExtendPubkeyFormat.ZPUB;
import static com.cobo.coinlib.ExtendPubkeyFormat.convertExtendPubkey;

public class BlueWalletExportFragment extends BaseFragment<BlueExportBinding> {
    @Override
    protected int setView() {
        return R.layout.blue_export;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        setQrCode();
        mBinding.info.setOnClickListener(v -> showBlueWalletInfo() );
        mBinding.done.setOnClickListener(v -> {
            if (mActivity instanceof SetupVaultActivity) {
                navigate(R.id.action_to_setupCompleteFragment);
            } else {
                MainActivity activity = (MainActivity) mActivity;
                activity.getNavController().popBackStack(R.id.assetFragment, false);
            }
        });
    }

    private void setQrCode() {
        try {
            JSONObject xpubInfo = GlobalViewModel.getXpubInfo(mActivity);
            String xpub = xpubInfo.getString("ExtPubKey");
            if (xpub.startsWith("xpub")) {
                xpub = convertExtendPubkey(xpub, ZPUB);
                xpubInfo.put("ExtPubKey",xpub);
            }
            mBinding.qrcode.setData(xpubInfo.toString());
            mBinding.enlarge.setOnClickListener(v -> mBinding.qrcode.showModal());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    private void showBlueWalletInfo() {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mActivity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.export_xpub_guide_text1_blue);
        binding.subTitle.setText(R.string.export_xpub_guide_text2_blue_info);
        binding.subTitle.setGravity(Gravity.START);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(mActivity.getSupportFragmentManager(), "");
    }
}
