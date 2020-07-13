package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.cobo.cold.R;
import com.cobo.cold.databinding.MultisigBottomSheetBinding;
import com.cobo.cold.databinding.MultisigMainBinding;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.fragment.BaseFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class MultisigMainFragment extends BaseFragment<MultisigMainBinding> {
    public static final String TAG = "MultisigMainFragment";

    @Override
    protected int setView() {
        return R.layout.multisig_main;
    }

    @Override
    protected void init(View view) {
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.asset_hasmore, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_scan:
                //scanQrCode();
                //break;
            case R.id.action_sdcard:
                //showFileList();
            case R.id.action_more:
                showBottomSheetMenu();
                //break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showBottomSheetMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        MultisigBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.multisig_bottom_sheet,null,false);
        binding.exportXpub.setOnClickListener(v-> {
            dialog.dismiss();
            navigate(R.id.export_export_multisig_expub);
        });
        binding.createMultisig.setOnClickListener(v-> {
            navigate(R.id.export_create_multisig_wallet);
            dialog.dismiss();
        });

        binding.importMultisig.setOnClickListener(v-> {
            dialog.dismiss();
        });

        binding.manageMultisig.setOnClickListener(v-> {
            dialog.dismiss();
        });

        dialog.setContentView(binding.getRoot());
        dialog.show();
    }
}
