package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModelProviders;

import com.cobo.cold.ui.fragment.BaseFragment;
import com.cobo.cold.viewmodel.MultiSigViewModel;

public abstract class MultiSigBaseFragment<T extends ViewDataBinding>
        extends BaseFragment<T> {
    protected MultiSigViewModel viewModel;

    @Override
    protected void init(View view) {
        viewModel = ViewModelProviders.of(mActivity).get(MultiSigViewModel.class);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
