package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.view.View;

import com.cobo.cold.R;
import com.cobo.cold.databinding.CollectExpubBinding;
import com.cobo.cold.ui.fragment.BaseFragment;

public class CollcetExpubFragment extends BaseFragment<CollectExpubBinding> {
    @Override
    protected int setView() {
        return R.layout.collect_expub;
    }

    @Override
    protected void init(View view) {
        mBinding.toolbar.setNavigationOnClickListener( v -> navigateUp());
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
