package com.cobo.cold.ui.fragment.multisig;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.cobo.cold.R;
import com.cobo.cold.databinding.ManageWalletBinding;
import com.cobo.cold.databinding.MultisigWalletItemBinding;
import com.cobo.cold.db.entity.MultiSigAddressEntity;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
import com.cobo.cold.ui.common.BaseBindingAdapter;

public class ManageWalletFragment extends MultiSigBaseFragment<ManageWalletBinding> {

    private Adapter adapter;
    @Override
    protected int setView() {
        return R.layout.manage_wallet;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> navigateUp());
        adapter = new Adapter(mActivity);
        mBinding.list.setAdapter(adapter);
        viewModel.getAllMultiSigWallet().observe(this, multiSigWalletEntities -> {
            adapter.setItems(multiSigWalletEntities);
        });
    }

    class Adapter extends BaseBindingAdapter<MultiSigWalletEntity, MultisigWalletItemBinding> {

        Adapter(Context context) {
            super(context);
        }
        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.multisig_wallet_item;
        }
        @Override
        protected void onBindItem(MultisigWalletItemBinding binding, MultiSigWalletEntity item) {
            binding.setWalletItem(item);
        }
    }
}
