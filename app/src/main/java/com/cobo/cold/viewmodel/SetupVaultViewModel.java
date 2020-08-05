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
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.MnemonicUtils;
import com.cobo.coinlib.utils.Bip39;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.callables.GetExtendedPublicKeyCallable;
import com.cobo.cold.callables.GetRandomEntropyCallable;
import com.cobo.cold.callables.GetVaultIdCallable;
import com.cobo.cold.callables.UpdatePassphraseCallable;
import com.cobo.cold.callables.WebAuthCallable;
import com.cobo.cold.callables.WriteMnemonicCallable;
import com.cobo.cold.db.entity.AccountEntity;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.util.HashUtil;

import org.spongycastle.util.encoders.Hex;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SetupVaultViewModel extends AndroidViewModel {

    private static final int VAULT_STATE_NOT_CREATE = 0;
    public static final int VAULT_STATE_CREATING = 1;
    public static final int VAULT_STATE_CREATED = 2;
    public static final int VAULT_STATE_CREATING_FAILED = 3;

    private final ObservableField<String> pwd1 = new ObservableField<>("");
    private final ObservableField<String> pwd2 = new ObservableField<>("");
    private final ObservableField<String> webAuthCode = new ObservableField<>("");
    private final ObservableField<Integer> mnemonicCount = new ObservableField<>(24);
    private final MutableLiveData<Integer> vaultCreateState = new MutableLiveData<>(VAULT_STATE_NOT_CREATE);
    private final MutableLiveData<String> mnemonic = new MutableLiveData<>("");
    private String vaultId;

    private final DataRepository mRepository;
    private String password;
    private String signature;

    public SetupVaultViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((MainApplication) application).getRepository();
    }

    public void calcAuthCode(String data) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String authCode = new WebAuthCallable(data).call();
            webAuthCode.set(format(authCode));
        });
    }

    private String format(String replace) {
        if (TextUtils.isEmpty(replace)) {
            return "";
        }
        String regex = "(.{4})";
        replace = replace.replaceAll(regex, "$1 ");
        return replace.trim();
    }

    public ObservableField<String> getWebAuthCode() {
        return webAuthCode;
    }

    public MutableLiveData<Integer> getVaultCreateState() {
        return vaultCreateState;
    }

    public ObservableField<String> getPwd1() {
        return pwd1;
    }

    public ObservableField<String> getPwd2() {
        return pwd2;
    }

    @NonNull
    public ObservableField<Integer> getMnemonicCount() {
        return mnemonicCount;
    }

    public void setMnemonicCount(int mnemonicCount) {
        this.mnemonicCount.set(mnemonicCount);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public boolean validateMnemonic(String mnemonic) {
        return Bip39.validateMnemonic(mnemonic);
    }

    public void writeMnemonic(String mnemonic) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            vaultCreateState.postValue(VAULT_STATE_CREATING);
            new WriteMnemonicCallable(mnemonic, password).call();
            vaultId = new GetVaultIdCallable().call();
            mRepository.clearDb();
            vaultCreateState.postValue(VAULT_STATE_CREATED);
            password = null;
        });
    }

    public void updatePassphrase(String passphrase) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            vaultCreateState.postValue(VAULT_STATE_CREATING);
            if (new UpdatePassphraseCallable(passphrase, password, signature).call()) {
                vaultId = new GetVaultIdCallable().call();
                deleteHiddenVaultData();
                vaultCreateState.postValue(VAULT_STATE_CREATED);
                password = null;
                signature = null;
            } else {
                vaultCreateState.postValue(VAULT_STATE_CREATING_FAILED);
            }

        });
    }

    public String getVaultId() {
        return vaultId;
    }

    public PasswordValidationResult validatePassword() {
        if (Objects.requireNonNull(pwd1.get()).length() < 10) {
            return PasswordValidationResult.RESULT_TOO_SHORT;
        } else if (!validInput(Objects.requireNonNull(pwd1.get()))) {
            return PasswordValidationResult.RESULT_INPUT_WRONG;
        } else {
            return PasswordValidationResult.RESULT_OK;
        }
    }

    private boolean validInput(String s) {
        char[] chars = s.toCharArray();
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        for (char c : chars) {
            if (Character.isDigit(c)) {
                hasDigit = true;
                continue;
            }
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
                continue;
            }
            if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            }
        }
        return hasDigit && hasLowerCase && hasUpperCase;
    }

    public void generateRandomMnemonic() {
        Executor executor = Executors.newSingleThreadExecutor();
        Runnable task = () -> {
            String entropy = new GetRandomEntropyCallable().call();
            if (!MnemonicUtils.isValidateEntropy(Hex.decode(entropy))) {
                this.mnemonic.postValue("");
            } else {
                this.mnemonic.postValue(Bip39.generateMnemonic(entropy));
            }
        };
        executor.execute(task);
    }

    public void generateMnemonicFromDiceRolls(byte[] diceRolls) {
        //Use the same algorithm as https://iancoleman.io/bip39/
        StringBuilder rolls = new StringBuilder();
        for (byte b: diceRolls) {
            rolls.append(b % 6);
        }
        String entropy = Hex.toHexString(Objects.requireNonNull(HashUtil.sha256(rolls.toString())));
        String mnemonic = Bip39.generateMnemonic(entropy);
        this.mnemonic.postValue(mnemonic);
    }

    public LiveData<String> getMnemonic() {
        return mnemonic;
    }

    public void presetData(List<CoinEntity> coins, final Runnable onComplete) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            for (CoinEntity coin : coins) {
                CoinEntity coinEntity = mRepository.loadCoinSync(coin.getCoinId());
                if (coinEntity != null) {
                    continue;
                }
                String xPub = new GetExtendedPublicKeyCallable(coin.getAccounts().get(0).getHdPath()).call();
                coin.setExPub(xPub);
                long id = mRepository.insertCoin(coin);
                coin.setId(id);
                boolean isFirstAccount = true;
                for (AccountEntity account : coin.getAccounts()) {
                    if (!isFirstAccount) {
                        xPub = new GetExtendedPublicKeyCallable(account.getHdPath()).call();
                    }
                    isFirstAccount = false;
                    account.setCoinId(id);
                    account.setExPub(xPub);
                    mRepository.insertAccount(account);
                    if (!Coins.showPublicKey(coin.getCoinCode())) {
                        new AddAddressViewModel.AddAddressTask(coin, mRepository, null)
                                .execute(coin.getCoinCode() + "-1");
                    }
                }
            }
            if (onComplete != null) {
                AppExecutors.getInstance().mainThread().execute(onComplete);
            }
        });
    }

    private void deleteHiddenVaultData() {
        mRepository.deleteHiddenVaultData();
    }

    public LiveData<List<CoinEntity>> getCoins() {
        return mRepository.reloadCoins();
    }

    public enum PasswordValidationResult {
        RESULT_OK,
        RESULT_NOT_MATCH,
        RESULT_TOO_SHORT,
        RESULT_INPUT_WRONG,
    }

}
