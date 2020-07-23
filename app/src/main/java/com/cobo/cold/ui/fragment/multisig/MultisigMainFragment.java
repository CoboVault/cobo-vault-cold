package com.cobo.cold.ui.fragment.multisig;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.databinding.AddAddressBottomSheetBinding;
import com.cobo.cold.databinding.MultisigBottomSheetBinding;
import com.cobo.cold.databinding.MultisigMainBinding;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.fragment.main.NumberPickerCallback;
import com.cobo.cold.ui.modal.ProgressModalDialog;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Objects;
import java.util.stream.IntStream;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class MultisigMainFragment extends MultiSigBaseFragment<MultisigMainBinding>
        implements NumberPickerCallback {
    public static final String TAG = "MultisigMainFragment";

    private Fragment[] fragments;
    private String[] title;
    private String coinId;
    private MultiSigWalletEntity wallet;
    private boolean isEmpty;
    private Menu mMenu;

    @Override
    protected int setView() {
        return R.layout.multisig_main;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        coinId = Utilities.isMainNet(mActivity) ? Coins.BTC.coinId() : Coins.XTN.coinId();
        mActivity.setSupportActionBar(mBinding.toolbar);
        mBinding.toolbar.setNavigationOnClickListener(((MainActivity) mActivity)::toggleDrawer);
        viewModel.getAllMultiSigWallet().observe(this, walletEntities -> {
            if (!walletEntities.isEmpty()) {
                isEmpty = false;
                wallet = walletEntities.get(0);
            } else {
                isEmpty = true;
            }
            refreshUI();
        });
    }

    private void refreshUI() {
        if (isEmpty) {
            mBinding.empty.setVisibility(View.VISIBLE);
            mBinding.fab.hide();
            mBinding.createMultisig.setOnClickListener( v ->navigate(R.id.create_multisig_wallet));
            mBinding.importMultisig.setOnClickListener( v ->navigate(R.id.import_multisig_file_list));
            if (mMenu != null) {
                MenuItem sdcard = mMenu.findItem(R.id.action_sdcard);
                if (sdcard != null) sdcard.setVisible(false);
                MenuItem scan = mMenu.findItem(R.id.action_scan);
                if (scan != null) scan.setVisible(false);
            }
        } else {
            mBinding.empty.setVisibility(View.GONE);
            mBinding.fab.show();
            mBinding.fab.setOnClickListener(v -> addAddress());
            mBinding.walletLabel.setText(wallet.getWalletName() + " >");
            mBinding.walletLabel.setOnClickListener(v -> navigateToManageWallet());
            title = new String[]{getString(R.string.tab_my_address), getString(R.string.tab_my_change_address)};
            initViewPager();
            if (mMenu != null) {
                MenuItem sdcard = mMenu.findItem(R.id.action_sdcard);
                if (sdcard != null) sdcard.setVisible(true);
                MenuItem scan = mMenu.findItem(R.id.action_scan);
                if (scan != null) scan.setVisible(true);
            }
        }
    }
    private void navigateToManageWallet() {
        Bundle data = new Bundle();
        data.putString("wallet_fingerprint",wallet.getWalletFingerPrint());
        navigate(R.id.action_to_multisig_wallet, data);
    }

    private void addAddress() {
        BottomSheetDialog dialog = new BottomSheetDialog(mActivity);
        AddAddressBottomSheetBinding binding = DataBindingUtil.inflate(LayoutInflater.from(mActivity),
                R.layout.add_address_bottom_sheet,null,false);
        String[] displayValue = IntStream.range(0, 9)
                .mapToObj(i -> String.valueOf(i + 1))
                .toArray(String[]::new);
        binding.setValue(1);
        binding.title.setText(getString(R.string.select_address_num, title[mBinding.tab.getSelectedTabPosition()]));
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.picker.setValue(0);
        binding.picker.setDisplayedValues(displayValue);
        binding.picker.setMinValue(0);
        binding.picker.setMaxValue(8);
        binding.picker.setOnValueChangedListener((picker, oldVal, newVal) -> binding.setValue(newVal + 1));
        binding.confirm.setOnClickListener(v-> {
            onValueSet(binding.picker.getValue() + 1);
            dialog.dismiss();

        });
        dialog.setContentView(binding.getRoot());
        dialog.show();
    }

    @Override
    public void onValueSet(int value) {
        ProgressModalDialog dialog = ProgressModalDialog.newInstance();
        dialog.show(Objects.requireNonNull(mActivity.getSupportFragmentManager()), "");
        Handler handler = new Handler();
        AppExecutors.getInstance().diskIO().execute(() -> {
            int tabPosition = mBinding.tab.getSelectedTabPosition();
            viewModel.addAddress(wallet.getWalletFingerPrint(), value, tabPosition);
            handler.post(() -> viewModel.getObservableAddState().observe(this, complete -> {
                if (complete) {
                    handler.postDelayed(dialog::dismiss, 500);
                }
            }));

        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        mMenu = menu;
        inflater.inflate(R.menu.asset_hasmore, menu);
        if (isEmpty) {
            menu.findItem(R.id.action_sdcard).setVisible(false);
            menu.findItem(R.id.action_scan).setVisible(false);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_scan:
                scanQrCode();
                break;
            case R.id.action_sdcard:
                Bundle data = new Bundle();
                data.putBoolean("multisig",true);
                navigate(R.id.action_to_psbtListFragment, data);
                break;
            case R.id.action_more:
                showBottomSheetMenu();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanQrCode() {

    }


    private void initViewPager() {
        if (fragments == null) {
            fragments = new Fragment[title.length];
            fragments[0] = MultiSigAddressFragment.newInstance(coinId, false, wallet.getWalletFingerPrint());
            fragments[1] = MultiSigAddressFragment.newInstance(coinId, true, wallet.getWalletFingerPrint());
        }
        mBinding.viewpager.setAdapter(new FragmentStatePagerAdapter(getChildFragmentManager(),
                BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments[position];
            }

            @Override
            public int getCount() {
                return title.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return title[position];
            }
        });
        mBinding.tab.setupWithViewPager(mBinding.viewpager);
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
            navigate(R.id.create_multisig_wallet);
            dialog.dismiss();
        });

        binding.importMultisig.setOnClickListener(v-> {
            navigate(R.id.import_multisig_file_list);
            dialog.dismiss();
        });

        binding.manageMultisig.setOnClickListener(v-> {
            navigate(R.id.manage_multisig_wallet);
            dialog.dismiss();
        });

        dialog.setContentView(binding.getRoot());
        dialog.show();
    }
}
