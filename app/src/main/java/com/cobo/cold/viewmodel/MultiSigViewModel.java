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
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cobo.coinlib.ExtendPubkeyFormat;
import com.cobo.coinlib.coins.BTC.Deriver;
import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.callables.GetExtendedPublicKeyCallable;
import com.cobo.cold.callables.GetMasterFingerprintCallable;
import com.cobo.cold.db.entity.MultiSigAddressEntity;
import com.cobo.cold.db.entity.MultiSigWalletEntity;
import com.cobo.cold.db.entity.TxEntity;
import com.cobo.cold.update.utils.FileUtils;
import com.cobo.cold.update.utils.Storage;
import com.cobo.cold.util.HashUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.cobo.coinlib.Util.getExpubFingerprint;
import static com.cobo.coinlib.utils.MultiSig.Account.P2SH;
import static com.cobo.coinlib.utils.MultiSig.Account.P2SH_TEST;
import static com.cobo.coinlib.utils.MultiSig.Account.P2WSH;
import static com.cobo.coinlib.utils.MultiSig.Account.P2SH_P2WSH;
import static com.cobo.coinlib.utils.MultiSig.Account.P2SH_P2WSH_TEST;
import static com.cobo.coinlib.utils.MultiSig.Account.P2WSH_TEST;

public class MultiSigViewModel extends AndroidViewModel {

    private final Storage storage;
    private final LiveData<List<MultiSigWalletEntity>> mObservableWallets;
    private final MutableLiveData<Boolean> addComplete = new MutableLiveData<>();
    private final Map<MultiSig.Account, String> xpubsMap = new HashMap<>();
    private final String xfp;
    private final DataRepository repo;

    public MultiSigViewModel(@NonNull Application application) {
        super(application);
        xfp = new GetMasterFingerprintCallable().call();
        repo = ((MainApplication) application).getRepository();
        storage = Storage.createByEnvironment(application);
        mObservableWallets = repo.loadAllMultiSigWallet();
    }

    public static String convertXpub(String xpub, MultiSig.Account account) {
        ExtendPubkeyFormat format = ExtendPubkeyFormat.valueOf(account.getXpubPrefix());
        return ExtendPubkeyFormat.convertExtendPubkey(xpub, format);
    }

    public static JSONObject decodeColdCardWalletFile(String content) {
        /*
        # Coldcard Multisig setup file (created on 5271C071)
        #
        Name: CC-2-of-3
        Policy: 2 of 3
        Derivation: m/48'/0'/0'/2'
        Format: P2WSH

        748CC6AA: xpub6F6iZVTmc3KMgAUkV9JRNaouxYYwChRswPN1ut7nTfecn6VPRYLXFgXar1gvPUX27QH1zaVECqVEUoA2qMULZu5TjyKrjcWcLTQ6LkhrZAj
        C2202A77: xpub6EiTGcKqBQy2uTat1QQPhYQWt8LGmZStNqKDoikedkB72sUqgF9fXLUYEyPthqLSb6VP4akUAsy19MV5LL8SvqdzvcABYUpKw45jA1KZMhm
        5271C071: xpub6EWksRHwPbDmXWkjQeA6wbCmXZeDPXieMob9hhbtJjmrmk647bWkh7om5rk2eoeDKcKG6NmD8nT7UZAFxXQMjTnhENTwTEovQw3MDQ8jJ16
         */

        content = content.replaceAll("P2WSH-P2SH", P2SH_P2WSH.getFormat());
        JSONObject object = new JSONObject();
        JSONArray xpubs = new JSONArray();
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{8}");
        boolean isTest = false;
        int total = 0;
        int threshold = 0;
        String path = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) {
                    if (line.contains("Coldcard")) {
                        object.put("Creator", "Coldcard");
                    } else if (line.contains("CoboVault")) {
                        object.put("Creator", "CoboVault");
                    }
                }
                String[] splits = line.split(": ");
                if (splits.length != 2) continue;
                String label = splits[0];
                String value = splits[1];
                if (label.equals("Name")) {
                    object.put(label, value);
                } else if (label.equals("Policy")) {
                    String[] policy = value.split(" of ");
                    if (policy.length == 2) {
                        threshold = Integer.parseInt(policy[0]);
                        total = Integer.parseInt(policy[1]);
                    }
                    object.put(label, value);
                } else if (label.equals("Format")) {
                    object.put(label, value);
                } else if (pattern.matcher(label).matches()) {
                    JSONObject xpub = new JSONObject();
                    if (ExtendPubkeyFormat.isValidXpub(value)) {
                        isTest = value.startsWith("tpub") || value.startsWith("Upub") || value.startsWith("Vpub");
                        object.put("isTest", isTest);
                        xpub.put("xfp", label);
                        xpub.put("xpub", convertXpub(value, MultiSig.Account.ofPath(path)));
                        xpubs.put(xpub);
                    }
                }
            }

            if (!isValidMultisigPolicy(total, threshold) || xpubs.length() != total) {
                Log.w("Multisig","invalid wallet policy");
                return null;
            }

            String format = object.getString("Format");

            boolean validDerivation = false;
            for (MultiSig.Account account : MultiSig.Account.values()) {
                if (account.isTest() == isTest && account.getFormat().equals(format)) {
                    validDerivation = true;
                    object.put("Derivation", account.getPath());
                    break;
                }
            }

            if (!validDerivation) {
                Log.w("Multisig","invalid wallet validDerivation");
                return null;
            }

            object.put("Xpubs", xpubs);

        } catch (IOException | JSONException | NumberFormatException e) {
            e.printStackTrace();
            Log.w("Multisig","invalid wallet ",e);
            return null;
        }

        return object;
    }

    private static boolean isValidMultisigPolicy(int total, int threshold) {
        return total <= 15 && total >= 2 && threshold <= total || threshold >= 1;
    }

    public LiveData<List<MultiSigWalletEntity>> getAllMultiSigWallet() {
        return mObservableWallets;
    }

    public LiveData<List<MultiSigAddressEntity>> getMultiSigAddress(String walletFingerprint) {
        return repo.loadAddressForWallet(walletFingerprint);
    }

    public LiveData<List<TxEntity>> loadTxs(String walletFingerprint) {
        return repo.loadMultisigTxs(walletFingerprint);
    }

    public LiveData<MultiSigWalletEntity> getCurrentWallet() {
        String netmode = Utilities.isMainNet(getApplication()) ? "main" : "testnet";
        MutableLiveData<MultiSigWalletEntity> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            String defaultMultisgWalletFp = Utilities.getDefaultMultisigWallet(getApplication(), xfp);
            if (!TextUtils.isEmpty(defaultMultisgWalletFp)) {
                MultiSigWalletEntity wallet = repo.loadMultisigWallet(defaultMultisgWalletFp);
                if (wallet != null && wallet.getNetwork().equals(netmode)) {
                    result.postValue(wallet);
                } else {
                    List<MultiSigWalletEntity> list = repo.loadAllMultiSigWalletSync();
                    if (!list.isEmpty()) {
                        result.postValue(list.get(0));
                        Utilities.setDefaultMultisigWallet(getApplication(), xfp, list.get(0).getWalletFingerPrint());
                    } else {
                        result.postValue(null);
                    }
                }
            } else {
                List<MultiSigWalletEntity> list = repo.loadAllMultiSigWalletSync();
                if (!list.isEmpty()) {
                    result.postValue(list.get(0));
                    Utilities.setDefaultMultisigWallet(getApplication(), xfp, list.get(0).getWalletFingerPrint());
                } else {
                    result.postValue(null);
                }
            }
        });
        return result;
    }

    public String getXpub(MultiSig.Account account) {
        if (!xpubsMap.containsKey(account)) {
            String expub = new GetExtendedPublicKeyCallable(account.getPath()).call();
            xpubsMap.put(account, convertXpub(expub, account));
        }
        return xpubsMap.get(account);
    }

    public LiveData<JSONObject> exportWalletToCaravan(String walletFingerprint) {
        MutableLiveData<JSONObject> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletFingerprint);
            if (wallet != null) {
                try {
                    String creator = wallet.getCreator();
                    String path = wallet.getExPubPath();
                    int threshold = wallet.getThreshold();
                    int total = wallet.getTotal();
                    boolean isTest = "testnet".equals(wallet.getNetwork());
                    JSONObject object = new JSONObject();
                    object.put("name", wallet.getWalletName());
                    object.put("addressType", MultiSig.Account.ofPath(path, isTest).getFormat());
                    object.put("network", isTest ? "testnet" : "mainnet");

                    JSONObject client = new JSONObject();
                    client.put("type", "public");
                    object.put("client", client);

                    JSONObject quorum = new JSONObject();
                    quorum.put("requiredSigners", threshold);
                    quorum.put("totalSigners", total);
                    object.put("quorum", quorum);

                    JSONArray extendedPublicKeys = new JSONArray();
                    JSONArray xpubArray = new JSONArray(wallet.getExPubs());
                    for (int i = 0; i < xpubArray.length(); i++) {
                        JSONObject xpubKey = new JSONObject();
                        xpubKey.put("name", "Extended Public Key " + (i + 1));
                        xpubKey.put("bip32Path", xpubArray.getJSONObject(i).optString("path", path));
                        String xpub = xpubArray.getJSONObject(i).getString("xpub");
                        if (isTest) {
                            xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.tpub);
                        } else {
                            xpub = ExtendPubkeyFormat.convertExtendPubkey(xpub, ExtendPubkeyFormat.xpub);
                        }
                        xpubKey.put("xpub", xpub);
                        xpubKey.put("xfp", xpubArray.getJSONObject(i).optString("xfp"));
                        xpubKey.put("method", "Caravan".equals(creator) ? xpubArray.getJSONObject(i).optString("method") : "text");
                        extendedPublicKeys.put(xpubKey);
                    }
                    object.put("extendedPublicKeys", extendedPublicKeys);
                    object.put("startingAddressIndex", 0);
                    result.postValue(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        return result;
    }

    public LiveData<String> exportWalletToCosigner(String walletFingerprint) {
        MutableLiveData<String> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletFingerprint);
            if (wallet != null) {
                try {
                    StringBuilder builder = new StringBuilder();
                    String path = wallet.getExPubPath();
                    int threshold = wallet.getThreshold();
                    int total = wallet.getTotal();
                    builder.append(String.format("# CoboVault Multisig setup file (created on %s)", xfp)).append("\n")
                            .append("#").append("\n")
                            .append("Name: ").append(wallet.getWalletName()).append("\n")
                            .append(String.format("Policy: %d of %d", threshold, total)).append("\n")
                            .append("Derivation: ").append(path).append("\n")
                            .append("Format: ").append(MultiSig.Account.ofPath(path).getFormat()).append("\n\n");
                    JSONArray xpubs = new JSONArray(wallet.getExPubs());
                    for (int i = 0; i < xpubs.length(); i++) {
                        JSONObject xpub = xpubs.getJSONObject(i);
                        builder.append(xpub.getString("xfp")).append(": ").append(xpub.getString("xpub")).append("\n");
                    }
                    result.postValue(builder.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        return result;
    }

    public LiveData<MultiSigWalletEntity> getWalletEntity(String walletFingerprint) {
        MutableLiveData<MultiSigWalletEntity> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletFingerprint);
            if (wallet != null) {
                result.postValue(wallet);
            }
        });
        return result;
    }

    public String getXfp() {
        return xfp;
    }

    public String getExportXpubInfo(MultiSig.Account account) {
        JSONObject object = new JSONObject();
        try {
            object.put("xfp", xfp);
            object.put("xpub", getXpub(account));
            object.put("path", account.getPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return object.toString();
    }

    public String getExportAllXpubInfo() {
        JSONObject object = new JSONObject();
        try {
            MultiSig.Account[] accounts = Utilities.isMainNet(getApplication()) ?
                    new MultiSig.Account[]{P2WSH, P2SH_P2WSH, P2SH} :
                    new MultiSig.Account[]{P2WSH_TEST, P2SH_P2WSH_TEST, P2SH_TEST};
            for (MultiSig.Account value : accounts) {
                String format = value.getFormat().toLowerCase().replace("-", "_");
                object.put(format + "_deriv", value.getPath());
                object.put(format, getXpub(value));
            }
            object.put("xfp", xfp.toUpperCase());
            return object.toString(2).replace("\\", "");
        } catch (JSONException e) {
            return "";
        }
    }

    public String getExportXpubFileName(MultiSig.Account account) {
        return xfp + "_" + account.getFormat() + ".json";
    }

    public String getExportAllXpubFileName() {
        return "ccxp-" + xfp + ".json";
    }

    public String getAddressTypeString(MultiSig.Account account) {
        int id = R.string.multi_sig_account_segwit;

        if (account == P2SH_P2WSH || account == P2SH_P2WSH_TEST) {
            id = R.string.multi_sig_account_p2sh;
        } else if (account == P2SH || account == P2SH_TEST) {
            id = R.string.multi_sig_account_legacy;
        }

        return getApplication().getString(id);
    }

    public LiveData<MultiSigWalletEntity> createMultisigWallet(int threshold,
                                                               MultiSig.Account account,
                                                               String name,
                                                               JSONArray xpubsInfo, String creator) throws XfpNotMatchException {
        MutableLiveData<MultiSigWalletEntity> result = new MutableLiveData<>();
        int total = xpubsInfo.length();
        boolean xfpMatch = false;
        List<String> xpubs = new ArrayList<>();
        for (int i = 0; i < xpubsInfo.length(); i++) {
            JSONObject obj;
            try {
                obj = xpubsInfo.getJSONObject(i);
                String xfp = obj.getString("xfp");
                String xpub = convertXpub(obj.getString("xpub"), account);
                if ((xfp.equalsIgnoreCase(this.xfp) || xfp.equalsIgnoreCase(getExpubFingerprint(getXpub(account))))
                        && ExtendPubkeyFormat.isEqualIgnorePrefix(getXpub(account), xpub)) {
                    xfpMatch = true;
                }
                xpubs.add(xpub);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (!xfpMatch) {
            throw new XfpNotMatchException("xfp not match");
        }
        String verifyCode = calculateWalletVerifyCode(threshold, xpubs, account.getPath());
        String walletFingerprint = verifyCode + xfp;
        String walletName = !TextUtils.isEmpty(name) ? name : "CV_" + verifyCode + "_" + threshold + "-" + total;
        MultiSigWalletEntity wallet = new MultiSigWalletEntity(
                walletName,
                threshold,
                total,
                account.getPath(),
                xpubsInfo.toString(),
                xfp,
                Utilities.isMainNet(getApplication()) ? "main" : "testnet", verifyCode, creator);
        wallet.setWalletFingerPrint(walletFingerprint);
        AppExecutors.getInstance().diskIO().execute(() -> {
            boolean exist = repo.loadMultisigWallet(walletFingerprint) != null;
            if (!exist) {
                repo.addMultisigWallet(wallet);
                new AddAddressTask(walletFingerprint, repo, null, 0).execute(1);
                new AddAddressTask(walletFingerprint, repo, () -> result.postValue(wallet), 1).execute(1);
            } else {
                repo.updateWallet(wallet);
                result.postValue(wallet);
            }
            Utilities.setDefaultMultisigWallet(getApplication(), xfp, walletFingerprint);
        });
        return result;
    }

    public void addAddress(String walletFingerprint, int number, int changeIndex) {
        addComplete.postValue(Boolean.FALSE);
        new AddAddressTask(walletFingerprint, repo, () -> addComplete.setValue(Boolean.TRUE), changeIndex).execute(number);
    }

    public String calculateWalletVerifyCode(int threshold, List<String> xpubs, String path) {
        String info = xpubs.stream()
                .map(s -> ExtendPubkeyFormat.convertExtendPubkey(s, ExtendPubkeyFormat.xpub))
                .sorted()
                .reduce((s1, s2) -> s1 + " " + s2)
                .orElse("") + threshold + "of" + xpubs.size() + path;
        return Hex.toHexString(Objects.requireNonNull(HashUtil.sha256(info))).substring(0, 8).toUpperCase();
    }

    public List<MultiSigAddressEntity> filterChangeAddress(List<MultiSigAddressEntity> entities) {
        return entities.stream()
                .filter(entity -> entity.getChangeIndex() == 1)
                .collect(Collectors.toList());
    }

    public List<MultiSigAddressEntity> filterReceiveAddress(List<MultiSigAddressEntity> entities) {
        return entities.stream()
                .filter(entity -> entity.getChangeIndex() == 0)
                .collect(Collectors.toList());
    }

    public LiveData<Boolean> getObservableAddState() {
        return addComplete;
    }

    public void deleteWallet(String walletFingerPrint) {
        repo.deleteMultisigWallet(walletFingerPrint);
        repo.deleteTxs(walletFingerPrint);
    }

    public LiveData<Map<String, JSONObject>> loadWalletFile() {
        MutableLiveData<Map<String, JSONObject>> result = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            Map<String, JSONObject> fileList = new HashMap<>();
            if (storage != null && storage.getExternalDir() != null) {
                File[] files = storage.getExternalDir().listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".txt")) {
                            JSONObject object = decodeColdCardWalletFile(FileUtils.readString(f));
                            if (object != null) {
                                fileList.put(f.getName(), object);
                            }
                        }
                        /*
                        else if (f.getName().endsWith(".json")) {
                            String fileContent = FileUtils.readString(f);
                            JSONObject object = decodeCaravanWalletFile(fileContent);
                            if (object != null) {
                                fileList.put(f.getName(), object);
                            }
                        }
                        */
                    }
                }
            }
            result.postValue(fileList);
        });
        return result;
    }

    public void updateWallet(MultiSigWalletEntity entity) {
        AppExecutors.getInstance().diskIO().execute(() -> repo.updateWallet(entity));
    }

    public static JSONObject decodeCaravanWalletFile(String content) {
        JSONObject result = new JSONObject();
        int total;
        int threshold;
        String format;
        boolean isTest;
        try {
            content = content.replaceAll("P2WSH-P2SH",P2SH_P2WSH.getFormat());
            JSONObject object = new JSONObject(content);

            //Creator
            result.put("Creator", "Caravan");

            //Name
            result.put("Name", object.getString("name"));

            //Policy
            JSONObject quorum = object.getJSONObject("quorum");
            threshold = quorum.getInt("requiredSigners");
            total = quorum.getInt("totalSigners");
            result.put("Policy", threshold + " of " + total);

            //Format
            format = object.getString("addressType");
            result.put("Format", format);

            //isTest
            isTest = "testnet".equals(object.getString("network"));
            result.put("isTest", isTest);

            //Derivation
            MultiSig.Account account = MultiSig.Account.ofFormat(format, isTest);
            if (account == null) {
                Log.w("Multisig","invalid format");
                return null;
            }
            result.put("Derivation", account.getPath());

            //Xpubs
            JSONArray xpubs = object.getJSONArray("extendedPublicKeys");
            JSONArray xpubsArray = new JSONArray();
            for (int i = 0; i < xpubs.length(); i++) {
                JSONObject xpubJson = new JSONObject();
                String xpub = xpubs.getJSONObject(i).getString("xpub");
                String path = xpubs.getJSONObject(i).getString("bip32Path");
                String xfp  = xpubs.getJSONObject(i).getString("xfp");
                String method  = xpubs.getJSONObject(i).getString("method");
                if (ExtendPubkeyFormat.isValidXpub(xpub)) {
                    if (xpub.startsWith("tpub") || xpub.startsWith("Upub") || xpub.startsWith("Vpub")) {
                        object.put("isTest", true);
                    }
                    if (TextUtils.isEmpty(xfp)) {
                        xfp = getExpubFingerprint(xpub);
                    }
                    xpubJson.put("path", path);
                    xpubJson.put("xfp", xfp);
                    xpubJson.put("xpub", convertXpub(xpub, account));
                    xpubJson.put("method", method);
                    xpubsArray.put(xpubJson);
                }
            }
            if (!isValidMultisigPolicy(total, threshold) || xpubs.length() != total) {
                Log.w("Multisig","invalid wallet Policy");
                return null;
            }
            result.put("Xpubs", xpubsArray);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("Multisig","invalid wallet ", e);
        }
        return null;
    }

    public static class AddAddressTask extends AsyncTask<Integer, Void, Void> {
        private final String walletFingerprint;
        private final DataRepository repo;
        private final Runnable onComplete;
        private final int changeIndex;

        AddAddressTask(String walletFingerprint,
                       DataRepository repo,
                       Runnable onComplete,
                       int changeIndex) {
            this.walletFingerprint = walletFingerprint;
            this.repo = repo;
            this.onComplete = onComplete;
            this.changeIndex = changeIndex;
        }

        @Override
        protected Void doInBackground(Integer... count) {
            boolean isMainNet = Utilities.isMainNet(MainApplication.getApplication());
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletFingerprint);
            List<MultiSigAddressEntity> address = repo.loadAddressForWalletSync(walletFingerprint);
            Optional<MultiSigAddressEntity> optional = address.stream()
                    .filter(addressEntity -> addressEntity.getPath()
                            .startsWith(wallet.getExPubPath() + "/" + changeIndex))
                    .max((o1, o2) -> o1.getIndex() - o2.getIndex());
            int index = -1;
            if (optional.isPresent()) {
                index = optional.get().getIndex();
            }
            List<MultiSigAddressEntity> entities = new ArrayList<>();
            int addressCount = index + 1;
            List<String> xpubList = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(wallet.getExPubs());
                for (int i = 0; i < jsonArray.length(); i++) {
                    xpubList.add(jsonArray.getJSONObject(i).getString("xpub"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Deriver deriver = new Deriver(isMainNet);
            for (int i = 0; i < count[0]; i++) {
                MultiSigAddressEntity multisigAddress = new MultiSigAddressEntity();
                String addr = deriver.deriveMultiSigAddress(wallet.getThreshold(),
                        xpubList, new int[]{changeIndex, addressCount + i},
                        MultiSig.Account.ofPath(wallet.getExPubPath()));
                multisigAddress.setPath(wallet.getExPubPath() + "/" + changeIndex + "/" + (addressCount + i));
                multisigAddress.setAddress(addr);
                multisigAddress.setIndex(i + addressCount);
                multisigAddress.setName("BTC-" + (i + addressCount));
                multisigAddress.setWalletFingerPrint(walletFingerprint);
                multisigAddress.setChangeIndex(changeIndex);
                entities.add(multisigAddress);
            }
            repo.insertMultisigAddress(entities);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
}


