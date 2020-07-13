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

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.cobo.coinlib.ExtendPubkeyFormat;
import com.cobo.coinlib.coins.BTC.Deriver;
import com.cobo.coinlib.exception.InvalidPathException;
import com.cobo.coinlib.path.AddressIndex;
import com.cobo.coinlib.path.CoinPath;
import com.cobo.coinlib.utils.MultiSig;
import com.cobo.cold.DataRepository;
import com.cobo.cold.MainApplication;
import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.callables.GetExtendedPublicKeyCallable;
import com.cobo.cold.callables.GetMasterFingerprintCallable;
import com.cobo.cold.db.entity.MultiSigAddressEntity;
import com.cobo.cold.db.entity.MultiSigWalletEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MultiSigViewModel extends AndroidViewModel {

    private Map<MultiSig.Account, String> xpubsMap = new HashMap<>();
    private String xfp;
    private DataRepository repo;
    public MultiSigViewModel(@NonNull Application application) {
        super(application);
        xfp = new GetMasterFingerprintCallable().call();
        repo = ((MainApplication)application).getRepository();
    }

    public String getXpub(MultiSig.Account account) {
        if (!xpubsMap.containsKey(account)) {
            String expub = new GetExtendedPublicKeyCallable(account.getPath()).call();
            if (account == MultiSig.Account.P2WSH) {
                expub = ExtendPubkeyFormat.convertExtendPubkey(expub, ExtendPubkeyFormat.Zpub);
            } else if (account == MultiSig.Account.P2SH_P2WSH) {
                expub = ExtendPubkeyFormat.convertExtendPubkey(expub, ExtendPubkeyFormat.Ypub);
            }
            xpubsMap.put(account, expub);
        }
        return xpubsMap.get(account);
    }

    public Map<MultiSig.Account, String> getAllXpubs() {
        for (MultiSig.Account value : MultiSig.Account.values()) {
            getXpub(value);
        }
        return xpubsMap;
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

    public String getExportXpubFileName(MultiSig.Account account) {
        return xfp + "-"+ account.getFormat() +".json";
    }

    public String getAddressTypeString(MultiSig.Account account) {
        int id = R.string.multi_sig_account_segwit;

        if (account == MultiSig.Account.P2SH_P2WSH) {
            id = R.string.multi_sig_account_p2sh;
        } else if (account == MultiSig.Account.P2SH) {
            id = R.string.multi_sig_account_legacy;
        }

        return getApplication().getString(id);
    }

    public void createMultisigWallet(int threshold,
                                     MultiSig.Account account,
                                     JSONArray xpubsInfo) throws XfpNotMatchException {
        int total = xpubsInfo.length();
        boolean xfpMatch = false;
        for (int i = 0; i < xpubsInfo.length(); i++) {
            JSONObject obj = null;
            try {
                obj = xpubsInfo.getJSONObject(i);
                String xfp = obj.getString("xfp");
                String xpub = obj.getString("xpub");
                if (xfp.equals(this.xfp) && getXpub(account).equals(xpub)) {
                    xfpMatch = true;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        if (!xfpMatch) {
            throw new XfpNotMatchException("xfp not match");
        }
        MultiSigWalletEntity wallet = new MultiSigWalletEntity(
                " " + threshold + "/" + total,
                threshold, total, account.getPath(), xpubsInfo.toString(),
                xfp, "main");
        long walletId = repo.addMultisigWallet(wallet);

        new AddAddressTask(walletId,repo,null,0).execute(1);
        new AddAddressTask(walletId,repo,null,1).execute(1);
    }

    public static class AddAddressTask extends AsyncTask<Integer, Void, Void> {
        private final long walletId;
        private final DataRepository repo;
        private final Runnable onComplete;
        private int changeIndex;

        AddAddressTask(long walletId,
                       DataRepository repo,
                       Runnable onComplete,
                       int changeIndex) {
            this.walletId = walletId;
            this.repo = repo;
            this.onComplete = onComplete;
            this.changeIndex = changeIndex;
        }

        @Override
        protected Void doInBackground(Integer... count) {
            boolean isMainNet = Utilities.isMainNet(MainApplication.getApplication());
            MultiSigWalletEntity wallet = repo.loadMultisigWallet(walletId);
            List<MultiSigAddressEntity> address = repo.loadAddressForWallet(walletId);
            Optional<MultiSigAddressEntity> optional = address.stream()
                    .filter(addressEntity -> addressEntity.getPath()
                            .startsWith(wallet.getExPubPath()+"/" + changeIndex))
                    .max((o1, o2) -> o1.getIndex() - o2.getIndex());
            int index = -1;
            if (optional.isPresent()) {
                try {
                    AddressIndex addressIndex = CoinPath.parsePath(optional.get().getPath());
                    index = addressIndex.getValue();
                } catch (InvalidPathException e) {
                    e.printStackTrace();
                }
            }
            List<MultiSigAddressEntity> entities = new ArrayList<>();
            int addressCount = index + 1;
            Deriver deriver = new Deriver(isMainNet);
            for (int i = 0; i < count[0]; i++) {
                MultiSigAddressEntity addressEntity = new MultiSigAddressEntity();
                String addr = deriver.deriveMultiSigAddress(wallet.getThreshold(),
                        new ArrayList<>(), new int[] {changeIndex, addressCount + i},
                        MultiSig.Account.ofPath(wallet.getExPubPath()));
                addressEntity.setPath(wallet.getExPubPath()+"/"+changeIndex+"/"+(addressCount + i));
                addressEntity.setAddress(addr);
                addressEntity.setIndex(i + addressCount);
                addressEntity.setName(isMainNet? "BTC" : "XTN"+ "-" + (i + addressCount));
                addressEntity.setWalletId(walletId);
                addressEntity.setChangeIndex(changeIndex);
                entities.add(addressEntity);
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


