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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.Util;
import com.cobo.coinlib.coins.AbsCoin;
import com.cobo.coinlib.coins.AbsTx;
import com.cobo.coinlib.coins.BTC.Btc;
import com.cobo.coinlib.coins.BTC.BtcImpl;
import com.cobo.coinlib.coins.BTC.Deriver;
import com.cobo.coinlib.coins.BTC.Electrum.ElectrumTx;
import com.cobo.coinlib.coins.BTC.UtxoTx;
import com.cobo.coinlib.exception.InvalidPathException;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.SignCallback;
import com.cobo.coinlib.interfaces.SignPsbtCallback;
import com.cobo.coinlib.interfaces.Signer;
import com.cobo.coinlib.path.AddressIndex;
import com.cobo.coinlib.path.CoinPath;
import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.callables.ClearTokenCallable;
import com.cobo.cold.callables.GetMessageCallable;
import com.cobo.cold.callables.GetPasswordTokenCallable;
import com.cobo.cold.callables.VerifyFingerprintCallable;
import com.cobo.cold.db.entity.AccountEntity;
import com.cobo.cold.db.entity.AddressEntity;
import com.cobo.cold.db.entity.CoinEntity;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.encryption.ChipSigner;
import com.cobo.cold.protobuf.TransactionProtoc;
import com.cobo.cold.ui.views.AuthenticateModal;
import com.googlecode.protobuf.format.JsonFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.Hex;

import java.security.SignatureException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static com.cobo.coinlib.coins.BTC.Electrum.TxUtils.isMasterPublicKeyMatch;
import static com.cobo.cold.viewmodel.AddAddressViewModel.AddAddressTask.getAddressType;
import static com.cobo.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.DUPLICATE_TX;
import static com.cobo.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.NORMAL;
import static com.cobo.cold.ui.fragment.main.FeeAttackChecking.FeeAttackCheckingResult.SAME_OUTPUTS;
import static com.cobo.cold.viewmodel.ElectrumViewModel.ELECTRUM_SIGN_ID;
import static com.cobo.cold.viewmodel.ElectrumViewModel.adapt;
import static com.cobo.cold.viewmodel.GlobalViewModel.getAccount;
import static com.cobo.cold.viewmodel.PsbtViewModel.WASABI_SIGN_ID;

public class TxConfirmViewModel extends AndroidViewModel {

    public static final String STATE_NONE = "";
    public static final String STATE_SIGNING = "signing";
    public static final String STATE_SIGN_FAIL = "signing_fail";
    public static final String STATE_SIGN_SUCCESS = "signing_success";
    public static final String TAG = "Vault.TxConfirm";
    private final DataRepository mRepository;
    private final MutableLiveData<TxEntity> observableTx = new MutableLiveData<>();
    private final MutableLiveData<Exception> parseTxException = new MutableLiveData<>();
    private final MutableLiveData<Boolean> addingAddress = new MutableLiveData<>();
    private final MutableLiveData<Integer> feeAttachCheckingResult = new MutableLiveData<>();
    private AbsTx transaction;
    private String coinCode;
    private final MutableLiveData<String> signState = new MutableLiveData<>();
    private AuthenticateModal.OnVerify.VerifyToken token;
    private TxEntity previousSignedTx;


    public TxConfirmViewModel(@NonNull Application application) {
        super(application);
        observableTx.setValue(null);
        mRepository = MainApplication.getApplication().getRepository();
    }

    public MutableLiveData<TxEntity> getObservableTx() {
        return observableTx;
    }

    public MutableLiveData<Exception> parseTxException() {
        return parseTxException;
    }

    public void parseTxData(String json) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            try {
                JSONObject object = new JSONObject(json);
                Log.i(TAG, "object = " + object.toString(4));
                transaction = AbsTx.newInstance(object);
                if (transaction == null) {
                    observableTx.postValue(null);
                    parseTxException.postValue(new InvalidTransactionException("invalid transaction"));
                    return;
                }
                if (transaction instanceof UtxoTx) {
                    if (!checkChangeAddress(transaction)) {
                        observableTx.postValue(null);
                        parseTxException.postValue(new InvalidTransactionException("invalid change address"));
                        return;
                    }
                }
                TxEntity tx = generateTxEntity(object);
                observableTx.postValue(tx);
                if (Coins.BTC.coinCode().equals(transaction.getCoinCode())) {
                    feeAttackChecking(tx);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    public TxEntity getPreviousSignTx() {
        return previousSignedTx;
    }

    private void feeAttackChecking(TxEntity txEntity) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            String inputs = txEntity.getFrom();
            String outputs = txEntity.getTo();
            List<TxEntity> txs = mRepository.loadAllTxSync(Coins.BTC.coinId());
            for (TxEntity tx : txs) {
                if (inputs.equals(tx.getFrom()) && outputs.equals(tx.getTo())) {
                    previousSignedTx = tx;
                    feeAttachCheckingResult.postValue(DUPLICATE_TX);
                    break;
                } else if (outputs.equals(tx.getTo())) {
                    feeAttachCheckingResult.postValue(SAME_OUTPUTS);
                    break;
                } else {
                    feeAttachCheckingResult.postValue(NORMAL);
                }
            }
        });
    }

    public LiveData<Integer> feeAttackChecking() {
        return feeAttachCheckingResult;
    }

    private TxEntity generateTxEntity(JSONObject object) throws JSONException {
        TxEntity tx = new TxEntity();
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(20);
        coinCode = Objects.requireNonNull(transaction).getCoinCode();
        tx.setSignId(object.getString("signId"));
        tx.setTimeStamp(object.optLong("timestamp"));
        tx.setCoinCode(coinCode);
        tx.setCoinId(Coins.coinIdFromCoinCode(coinCode));
        tx.setFrom(getFromAddress());
        tx.setTo(getToAddress());
        tx.setAmount(nf.format(transaction.getAmount()) + " " + transaction.getUnit());
        tx.setFee(nf.format(transaction.getFee()) + " " + coinCode);
        tx.setMemo(transaction.getMemo());
        tx.setBelongTo(mRepository.getBelongTo());
        return tx;
    }

    public void parseTxnData(String txnData) {
        AppExecutors.getInstance().networkIO().execute(() -> {
            try {
                CoinEntity coinEntity = mRepository.loadCoinSync(Coins.BTC.coinId());
                AccountEntity accountEntity =
                        mRepository.loadAccountsByPath(coinEntity.getId(), getAccount(getApplication()).getPath());

                if (accountEntity != null) {
                    String xpub = accountEntity.getExPub();
                    ElectrumTx tx = ElectrumTx.parse(Hex.decode(txnData));

                    if (!isMasterPublicKeyMatch(xpub, tx)) {
                        throw new XpubNotMatchException("xpub not match");
                    }

                    JSONObject signTx = parseElectrumTxHex(tx);
                    parseTxData(signTx.toString());
                }
            } catch (ElectrumTx.SerializationException | JSONException | DecoderException e) {
                e.printStackTrace();
                parseTxException.postValue(new InvalidTransactionException("invalid transaction"));
            } catch (XpubNotMatchException e) {
                e.printStackTrace();
                parseTxException.postValue(new XpubNotMatchException("invalid transaction"));
            }
        });
    }

    private JSONObject parseElectrumTxHex(ElectrumTx tx) throws JSONException {
        JSONObject btcTx = adapt(tx);
        TransactionProtoc.SignTransaction.Builder builder = TransactionProtoc.SignTransaction.newBuilder();
        builder.setCoinCode(Coins.BTC.coinCode())
                .setSignId(ELECTRUM_SIGN_ID)
                .setTimestamp(generateAutoIncreaseId())
                .setDecimal(8);
        String signTransaction = new JsonFormat().printToString(builder.build());
        JSONObject signTx = new JSONObject(signTransaction);
        signTx.put("btcTx", btcTx);
        return signTx;
    }


    public void parsePsbtBase64(String psbtBase64) {
        AppExecutors.getInstance().networkIO().execute(() -> {
            Btc btc = new Btc(new BtcImpl());
            JSONObject psbtTx = btc.parsePsbt(psbtBase64);
            if (psbtTx == null) {
                parseTxException.postValue(new InvalidTransactionException("parse failed,invalid psbt data"));
                return;
            }

            try {
                JSONObject adaptTx = PsbtViewModel.adapt(psbtTx);
                if (adaptTx.getJSONArray("inputs").length() == 0) {
                    parseTxException.postValue(
                            new InvalidTransactionException("master fingerprint not match, or nothing can be sign"));
                }
                JSONObject signTx = parseWasabiTx(adaptTx);
                parseTxData(signTx.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                parseTxException.postValue(new InvalidTransactionException("adapt failed,invalid psbt data"));
            } catch (WatchWalletNotMatchException e) {
                e.printStackTrace();
                parseTxException.postValue(e);
            }

        });
    }

    private JSONObject parseWasabiTx(JSONObject adaptTx) throws JSONException {
        TransactionProtoc.SignTransaction.Builder builder = TransactionProtoc.SignTransaction.newBuilder();
        builder.setCoinCode(Coins.BTC.coinCode())
                .setSignId(WASABI_SIGN_ID)
                .setTimestamp(generateAutoIncreaseId())
                .setDecimal(8);
        String signTransaction = new JsonFormat().printToString(builder.build());
        JSONObject signTx = new JSONObject(signTransaction);
        signTx.put("btcTx", adaptTx);
        return signTx;
    }

    private long generateAutoIncreaseId() {
        List<TxEntity> txEntityList = mRepository.loadElectrumTxsSync(Coins.BTC.coinId());
        if (txEntityList == null || txEntityList.isEmpty()) {
            return 0;
        }
        return txEntityList.stream()
                .max(Comparator.comparing(TxEntity::getTimeStamp))
                .get()
                .getTimeStamp() + 1;
    }

    private boolean checkChangeAddress(AbsTx utxoTx) {
        UtxoTx.ChangeAddressInfo changeAddressInfo = ((UtxoTx) utxoTx).getChangeAddressInfo();
        if (changeAddressInfo == null) {
            return true;
        }
        String hdPath = changeAddressInfo.hdPath;
        String address = changeAddressInfo.address;

        String accountHdPath = getAccountHdPath(changeAddressInfo.hdPath);

        AccountEntity accountEntity = getAccountEntityByPath(accountHdPath,
                mRepository.loadCoinEntityByCoinCode(utxoTx.getCoinCode()));
        if (accountEntity == null) {
            return false;
        }
        String exPub = accountEntity.getExPub();
        Deriver deriver = new Deriver();

        try {
            AddressIndex addressIndex = CoinPath.parsePath(hdPath);
            int change = addressIndex.getParent().getValue();
            int index = addressIndex.getValue();
            String expectAddress = Objects.requireNonNull(deriver).derive(exPub, change,
                    index, getAddressType(accountEntity));
            return address.equals(expectAddress);
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getToAddress() {
        String to = transaction.getTo();

        if (transaction instanceof UtxoTx) {
            JSONArray outputs = ((UtxoTx) transaction).getOutputs();
            if (outputs != null) {
                return outputs.toString();
            }
        }

        return to;
    }

    private String getFromAddress() {
        if (!TextUtils.isEmpty(transaction.getFrom())) {
            return transaction.getFrom();
        }
        String[] paths = transaction.getHdPath().split(AbsTx.SEPARATOR);
        String[] externalPath = Stream.of(paths)
                .filter(this::isExternalPath)
                .toArray(String[]::new);
        ensureAddressExist(externalPath);

        try {
            if (transaction instanceof UtxoTx) {
                JSONArray inputsClone = new JSONArray();
                JSONArray inputs = ((UtxoTx) transaction).getInputs();

                CoinEntity coinEntity = mRepository.loadCoinSync(Coins.coinIdFromCoinCode(transaction.getCoinCode()));
                AccountEntity accountEntity =
                        mRepository.loadAccountsByPath(coinEntity.getId(), getAccount(getApplication()).getPath());

                for (int i = 0; i < inputs.length(); i++) {
                    JSONObject input = inputs.getJSONObject(i);
                    long value = input.getJSONObject("utxo").getLong("value");
                    String hdpath = input.getString("ownerKeyPath");
                    AddressIndex addressIndex = CoinPath.parsePath(hdpath);
                    int index = addressIndex.getValue();
                    int change = addressIndex.getParent().getValue();

                    String from = new Deriver().derive(accountEntity.getExPub()
                            ,change,index, getAddressType(accountEntity));
                    inputsClone.put(new JSONObject().put("value", value)
                                                    .put("address",from));
                }

                return inputsClone.toString();
            }
        } catch (JSONException | InvalidPathException e) {
            e.printStackTrace();
        }

        return Stream.of(externalPath)
                .distinct()
                .map(path -> mRepository.loadAddressBypath(path).getAddressString())
                .reduce((s1, s2) -> s1 + AbsTx.SEPARATOR + s2)
                .orElse("");
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;

    }

    private boolean isExternalPath(@NonNull String path) {
        try {
            return CoinPath.parsePath(path).getParent().isExternal();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void ensureAddressExist(String[] paths) {
        if (paths == null || paths.length == 0) {
            return;
        }
        String maxIndexHdPath = paths[0];
        if (paths.length > 1) {
            int max = getAddressIndex(paths[0]);
            for (String path : paths) {
                if (getAddressIndex(path) > max) {
                    max = getAddressIndex(path);
                    maxIndexHdPath = path;
                }
            }
        }
        AddressEntity address = mRepository.loadAddressBypath(maxIndexHdPath);
        if (address == null) {
            addAddress(maxIndexHdPath);
        }
    }

    public LiveData<Boolean> getAddingAddressState() {
        return addingAddress;
    }

    private void addAddress(String hdPath) {
        String accountHdPath;
        int pathIndex;
        try {
            AddressIndex index = CoinPath.parsePath(hdPath);
            pathIndex = index.getValue();
            accountHdPath = index.getParent().getParent().toString();
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return;
        }

        CoinEntity coin = mRepository.loadCoinSync(Coins.coinIdFromCoinCode(coinCode));
        AccountEntity accountEntity = getAccountEntityByPath(accountHdPath, coin);
        if (accountEntity == null) return;

        List<AddressEntity> addressEntities = mRepository.loadAddressSync(coin.getCoinId());
        Optional<AddressEntity> addressEntityOptional = addressEntities
                .stream()
                .filter(addressEntity -> addressEntity.getPath()
                .startsWith(accountEntity.getHdPath()+"/" + 0))
                .max((o1, o2) -> o1.getPath().compareTo(o2.getPath()));

        int index = -1;
        if (addressEntityOptional.isPresent()) {
            try {
                AddressIndex addressIndex = CoinPath.parsePath(addressEntityOptional.get().getPath());
                index = addressIndex.getValue();
            } catch (InvalidPathException e) {
                e.printStackTrace();
            }
        }

        if (index < pathIndex) {
            final CountDownLatch mLatch = new CountDownLatch(1);
            addingAddress.postValue(true);
            new AddAddressViewModel.AddAddressTask(coin, mRepository, mLatch::countDown,
                    accountEntity.getExPub(), 0).execute(pathIndex - index);
            try {
                mLatch.await();
                addingAddress.postValue(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private AccountEntity getAccountEntityByPath(String accountHdPath, CoinEntity coin) {
        List<AccountEntity> accountEntities = mRepository.loadAccountsForCoin(coin);
        Optional<AccountEntity> optional = accountEntities.stream()
                .filter(accountEntity ->
                        accountEntity.getHdPath().equals(accountHdPath.toUpperCase()))
                .findFirst();

        AccountEntity accountEntity;
        if (optional.isPresent()) {
            accountEntity = optional.get();
        } else {
            return null;
        }
        return accountEntity;
    }

    private int getAddressIndex(String hdPath) {
        try {
            return CoinPath.parsePath(hdPath).getValue();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public MutableLiveData<String> getSignState() {
        return signState;
    }

    public void setToken(AuthenticateModal.OnVerify.VerifyToken token) {
        this.token = token;
    }

    public void handleSign() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer[] signer = initSigners();
            SignCallback callback = initSignCallback();
            signTransaction(transaction, callback, signer);
        });
    }

    public void handleSignPsbt(String psbt) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Signer[] signer = initSigners();
            SignPsbtCallback callback = new SignPsbtCallback() {
                @Override
                public void startSign() {
                    signState.postValue(STATE_SIGNING);
                }

                @Override
                public void onFail() {
                    signState.postValue(STATE_SIGN_FAIL);
                    new ClearTokenCallable().call();
                }

                @Override
                public void onSuccess(String txId, String psbtB64) {
                    TxEntity tx = observableTx.getValue();
                    Objects.requireNonNull(tx).setTxId(txId);
                    tx.setSignedHex(psbtB64);
                    mRepository.insertTx(tx);
                    signState.postValue(STATE_SIGN_SUCCESS);
                    new ClearTokenCallable().call();
                }

                @Override
                public void postProgress(int progress) {

                }
            };
            callback.startSign();
            Btc btc = new Btc(new BtcImpl());
            btc.signPsbt(psbt, callback, signer);
        });
    }


    private SignCallback initSignCallback() {
        return new SignCallback() {
            @Override
            public void startSign() {
                signState.postValue(STATE_SIGNING);
            }

            @Override
            public void onFail() {
                signState.postValue(STATE_SIGN_FAIL);
                new ClearTokenCallable().call();
            }

            @Override
            public void onSuccess(String txId, String rawTx) {
                TxEntity tx = observableTx.getValue();
                Objects.requireNonNull(tx).setTxId(txId);
                tx.setSignedHex(rawTx);
                mRepository.insertTx(tx);
                signState.postValue(STATE_SIGN_SUCCESS);
                new ClearTokenCallable().call();
            }

            @Override
            public void postProgress(int progress) {

            }
        };
    }

    private void signTransaction(@NonNull AbsTx transaction, @NonNull SignCallback callback, Signer... signer) {
        callback.startSign();
        if (signer == null) {
            callback.onFail();
            return;
        }
        switch (transaction.getTxType()) {
            case "OMNI":
            case "OMNI_USDT":
                Btc btc = new Btc(new BtcImpl());
                btc.generateOmniTx(transaction, callback, signer);
                break;
            default:
                AbsCoin coin = AbsCoin.newInstance(coinCode);
                Objects.requireNonNull(coin).generateTransaction(transaction, callback, signer);
        }
    }

    private Signer[] initSigners() {
        String[] paths = transaction.getHdPath().split(AbsTx.SEPARATOR);
        String coinCode = transaction.getCoinCode();
        String[] distinctPaths = Stream.of(paths).distinct().toArray(String[]::new);
        Signer[] signer = new Signer[distinctPaths.length];

        String authToken = getAuthToken();
        if (TextUtils.isEmpty(authToken)) {
            Log.w(TAG,"authToken null");
            return null;
        }
        CoinEntity coinEntity = mRepository.loadCoinEntityByCoinCode(coinCode);
        for (int i = 0; i < distinctPaths.length; i++) {
            String accountHdPath = getAccountHdPath(distinctPaths[i]);
            if (accountHdPath == null) {
                return null;
            }
            AccountEntity accountEntity = getAccountEntityByPath(accountHdPath,coinEntity);
            if (accountEntity == null) {
                return null;
            }
            String pubKey = Util.getPublicKeyHex(accountEntity.getExPub(), distinctPaths[i]);
            signer[i] = new ChipSigner(distinctPaths[i].toLowerCase(), authToken, pubKey);
        }
        return signer;
    }

    private String getAccountHdPath(String addressPath) {
        String accountHdPath;
        try {
            accountHdPath = CoinPath.parsePath(addressPath).getParent().getParent().toString();
        } catch (InvalidPathException e) {
            e.printStackTrace();
            return null;
        }
        return accountHdPath;
    }

    private String getAuthToken() {
        String authToken = null;
        if (!TextUtils.isEmpty(token.password)) {
            authToken = new GetPasswordTokenCallable(token.password).call();
        } else if(token.signature != null) {
            String message = new GetMessageCallable().call();
            if (!TextUtils.isEmpty(message)) {
                try {
                    token.signature.update(Hex.decode(message));
                    byte[] signature = token.signature.sign();
                    byte[] rs = Util.decodeRSFromDER(signature);
                    if (rs != null) {
                        authToken = new VerifyFingerprintCallable(Hex.toHexString(rs)).call();
                    }
                } catch (SignatureException e) {
                    e.printStackTrace();
                }
            }
        }
        AuthenticateModal.OnVerify.VerifyToken.invalid(token);
        return authToken;
    }

    public String getTxId() {
        return Objects.requireNonNull(observableTx.getValue()).getTxId();
    }

    public String getTxHex() {
        return Objects.requireNonNull(observableTx.getValue()).getSignedHex();
    }

    private final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    public boolean isAddressInWhiteList(String address) {
        try {
            return sExecutor.submit(() -> mRepository.queryWhiteList(address) != null).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
