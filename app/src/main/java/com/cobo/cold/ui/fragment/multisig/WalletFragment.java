package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.view.View;

import com.cobo.cold.R;
import com.cobo.cold.databinding.MultisigWalletBinding;

import java.util.Objects;

public class WalletFragment extends MultiSigBaseFragment<MultisigWalletBinding>
        implements ClickHandler {
    @Override
    protected int setView() {
        return R.layout.multisig_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        mBinding.setClickHandler(this);
    }

    @Override
    public void onClick(int id) {
        Bundle data = getArguments();
        Objects.requireNonNull(data).putBoolean("setup",false);
        data.putBoolean("multisig",true);
        navigate(id, data);
    }

}

