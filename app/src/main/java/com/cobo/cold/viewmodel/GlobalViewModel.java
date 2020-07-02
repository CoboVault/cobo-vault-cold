/*
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cobo.cold.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.coins.BTC.Btc;
import com.cobo.coinlib.coins.BTC.Deriver;
import com.cobo.coinlib.exception.InvalidPathException;
import com.cobo.coinlib.path.Account;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.BuildConfig;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.callables.GetExtendedPublicKeyCallable;
import com.cobo.cold.callables.GetMasterFingerprintCallable;
import com.cobo.cold.databinding.CommonModalBinding;
import com.cobo.cold.db.entity.AccountEntity;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.ui.modal.ExportToSdcardDialog;
import com.cobo.cold.ui.modal.ModalDialog;
import com.cobo.cold.update.utils.FileUtils;
import com.cobo.cold.update.utils.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.cobo.coinlib.ExtendPubkeyFormat.ypub;
import static com.cobo.coinlib.ExtendPubkeyFormat.zpub;
import static com.cobo.coinlib.ExtendPubkeyFormat.convertExtendPubkey;
import static com.cobo.cold.ui.fragment.setting.MainPreferenceFragment.SETTING_ADDRESS_FORMAT;

public class GlobalViewModel extends AndroidViewModel {

    private static final int DEFAULT_CHANGE_ADDRESS_NUM = 100;

    private final DataRepository mRepo;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sp, key) -> {
        if (SETTING_ADDRESS_FORMAT.equals(key)) {
            deriveChangeAddress();
        }
    };
    private MutableLiveData<String> exPub = new MutableLiveData<>();
    private MutableLiveData<List<String>> changeAddress = new MutableLiveData<>();
    private String xpub;
    private AccountEntity accountEntity;

    public GlobalViewModel(@NonNull Application application) {
        super(application);
        mRepo = MainApplication.getApplication().getRepository();
        deriveChangeAddress();
        Utilities.getPrefs(application).registerOnSharedPreferenceChangeListener(listener);
    }

    public static Coins.Account getAccount(Context context) {
        boolean isMainNet = Utilities.isMainNet(context);
        SharedPreferences pref = Utilities.getPrefs(context);
        String type = pref.getString(SETTING_ADDRESS_FORMAT, Coins.Account.P2SH.getType());
        for (Coins.Account account: Coins.Account.values()) {
            if (type.equals(account.getType()) && isMainNet == account.isMainNet()) {
                return account;
            }
        }
        return Coins.Account.P2SH;
    }

    public static Btc.AddressType getAddressType(Context context) {
        switch (getAccount(context)) {
            case P2SH:
            case P2SH_TESTNET:
                return Btc.AddressType.P2SH;
            case P2PKH:
            case P2PKH_TESTNET:
                return Btc.AddressType.P2PKH;
            case SegWit:
            case SegWit_TESTNET:
                return Btc.AddressType.SegWit;
        }
        return Btc.AddressType.SegWit;
    }


    public static String getAddressFormat(Context context) {
        switch (getAccount(context)) {
            case SegWit:
            case SegWit_TESTNET:
                return context.getString(R.string.native_segwit);
            case P2PKH:
            case P2PKH_TESTNET:
                return context.getString(R.string.p2pkh);
            case P2SH:
            case P2SH_TESTNET:
                return context.getString(R.string.nested_segwit);
        }
        return context.getString(R.string.nested_segwit);
    }

    public static JSONObject getXpubInfo(Context activity) {
        JSONObject xpubInfo = new JSONObject();
        Coins.Account account = getAccount(activity);
        String xpub = new GetExtendedPublicKeyCallable(account.getPath()).call();
        String masterKeyFingerprint = new GetMasterFingerprintCallable().call();
        try {
            xpubInfo.put("ExtPubKey", xpub);
            xpubInfo.put("MasterFingerprint", masterKeyFingerprint);
            xpubInfo.put("AccountKeyPath", account.getPath().substring(2));
            xpubInfo.put("CoboVaultFirmwareVersion", BuildConfig.VERSION_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return xpubInfo;
    }

    private void deriveChangeAddress() {
        AppExecutors.getInstance().networkIO().execute(()->{
            ExpubInfo expubInfo  = new ExpubInfo().getExPubInfo();
            xpub = expubInfo.expub;
            String path = expubInfo.hdPath;
            List<String> changes = new ArrayList<>();
            Btc.AddressType type;
            if (Coins.Account.P2SH.getPath().equals(path)
                    || Coins.Account.P2SH_TESTNET.getPath().equals(path)) {
                type = Btc.AddressType.P2SH;
            } else if (Coins.Account.SegWit.getPath().equals(path)
                    || Coins.Account.SegWit_TESTNET.getPath().equals(path)) {
                type = Btc.AddressType.SegWit;
            } else {
                type = Btc.AddressType.P2PKH;
            }

            Deriver btcDeriver = new Deriver(Utilities.isMainNet(getApplication()));
            for (int i = 0; i < DEFAULT_CHANGE_ADDRESS_NUM; i++) {
                changes.add(btcDeriver.derive(xpub,1, i, type));
            }
            changeAddress.postValue(changes);
        });
    }

    public LiveData<String> getExtendPublicKey() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            ExpubInfo expubInfo = new ExpubInfo().getExPubInfo();
            String hdPath = expubInfo.getHdPath();
            String extPub = expubInfo.getExpub();
            try {
                Account account = Account.parseAccount(hdPath);
                if (account.getParent().getParent().getValue() == 49
                        && extPub.startsWith("xpub")) {
                    exPub.postValue(convertExtendPubkey(extPub, ypub));
                } else if (extPub.startsWith("ypub")) {
                    exPub.postValue(extPub);
                } else if (account.getParent().getParent().getValue() == 84
                        && extPub.startsWith("xpub")) {
                    exPub.postValue(convertExtendPubkey(extPub, zpub));
                }
            } catch (InvalidPathException e) {
                e.printStackTrace();
            }

        });
        return exPub;
    }

    public AccountEntity getAccountEntity() {
        return accountEntity;
    }

    public LiveData<List<String>> getChangeAddress() {
        deriveChangeAddress();
        return changeAddress;
    }

    public String getXpub() {
        return xpub;
    }

    public static boolean hasSdcard(Context context) {
        Storage storage = Storage.createByEnvironment(context);
        return storage != null && storage.getExternalDir() != null;
    }

    public static boolean writeToSdcard(Storage storage, String content, String fileName) {
        File file = new File(storage.getElectrumDir(), fileName);
        return FileUtils.writeString(file, content);
    }

    public static void showNoSdcardModal(AppCompatActivity activity) {
        ModalDialog modalDialog = ModalDialog.newInstance();
        CommonModalBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(activity), R.layout.common_modal,
                null, false);
        binding.title.setText(R.string.hint);
        binding.subTitle.setText(R.string.insert_sdcard_hint);
        binding.close.setVisibility(View.GONE);
        binding.confirm.setText(R.string.know);
        binding.confirm.setOnClickListener(vv -> modalDialog.dismiss());
        modalDialog.setBinding(binding);
        modalDialog.show(activity.getSupportFragmentManager(), "");
    }

    public static void exportSuccess(AppCompatActivity activity, Runnable runnable) {
        ExportToSdcardDialog dialog = new ExportToSdcardDialog();
        dialog.show(activity.getSupportFragmentManager(), "");
        new Handler().postDelayed(() -> {
            dialog.dismiss();
            if (runnable != null) {
                runnable.run();
            }
        }, 1000);
    }


    private class ExpubInfo {
        private String hdPath;
        private String expub;

        public String getHdPath() {
            return hdPath;
        }

        public String getExpub() {
            return expub;
        }

        public ExpubInfo getExPubInfo() {
            CoinEntity coinEntity = mRepo.loadCoinSync(Utilities.currentCoin(getApplication()).coinId());
            SharedPreferences sp = Utilities.getPrefs(getApplication());
            List<AccountEntity> accounts = mRepo.loadAccountsForCoin(coinEntity);
            String format = sp.getString(SETTING_ADDRESS_FORMAT, Coins.Account.P2SH.getType());

            Coins.Account account;
            boolean isMainNet = Utilities.isMainNet(getApplication());
            if (Coins.Account.P2SH.getType().equals(format)) {
                account = isMainNet ? Coins.Account.P2SH : Coins.Account.P2SH_TESTNET;
            } else if(Coins.Account.SegWit.getType().equals(format)) {
                account = isMainNet ? Coins.Account.SegWit : Coins.Account.SegWit_TESTNET;
            } else {
                account = isMainNet ? Coins.Account.P2PKH : Coins.Account.P2PKH_TESTNET;
            }
            for (AccountEntity entity : accounts) {
                if (entity.getHdPath().equals(account.getPath())) {
                    accountEntity = entity;
                    hdPath = entity.getHdPath();
                    expub = entity.getExPub();

                }
            }
            return this;
        }
    }
}
