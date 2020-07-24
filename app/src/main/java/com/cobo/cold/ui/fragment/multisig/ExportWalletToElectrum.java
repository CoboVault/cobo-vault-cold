package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.view.View;

import com.cobo.cold.R;
import com.cobo.cold.databinding.ExportWalletToElectrumBinding;

import java.util.Objects;

public class ExportWalletToElectrum extends MultiSigBaseFragment<ExportWalletToElectrumBinding> {
    @Override
    protected int setView() {
        return R.layout.export_wallet_to_electrum;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        Bundle data = getArguments();
        Objects.requireNonNull(data);
        View.OnClickListener naviBack = v -> popBackStack(R.id.multisigFragment, false);
        if (data.getBoolean("isImportMultisig")) {
            mBinding.toolbar.setNavigationOnClickListener(naviBack);
        } else {
            mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        }
        mBinding.export.setOnClickListener(v -> export());
        viewModel.getWalletEntity(data.getString("wallet_fingerprint")).observe(this,
                wallet -> mBinding.text2.setText(getString(R.string.export_multisig_wallet_to_electrum_guide,
                wallet.getTotal(),wallet.getThreshold())));
        mBinding.exportLater.setOnClickListener(naviBack);

    }

    private void export() {
        navigate(R.id.action_to_export_multisig_xpub_to_el, getArguments());
    }
}
