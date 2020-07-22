package com.cobo.cold.db.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.cobo.coinlib.coins.BTC.Deriver;
import com.cobo.coinlib.utils.MultiSig;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "multi_sig_wallet", indices = {@Index("walletFingerPrint")})
public class MultiSigWalletEntity {
    @PrimaryKey
    @NonNull
    private String walletFingerPrint;
    private String walletName;
    @NonNull
    private int threshold;
    @NonNull
    private int total;
    @NonNull
    private String exPubPath;
    @NonNull
    private String exPubs;
    @NonNull
    private String belongTo;
    @NonNull
    private String verifyCode;
    @NonNull
    private String network;

    public MultiSigWalletEntity(String walletName, int threshold, int total,
                                String exPubPath, String exPubs, String belongTo,
                                String network, String verifyCode) {
        this.walletName = walletName;
        this.threshold = threshold;
        this.total = total;
        this.exPubPath = exPubPath;
        this.exPubs = exPubs;
        this.belongTo = belongTo;
        this.network = network;
        this.verifyCode = verifyCode;
    }

    @NonNull
    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(@NonNull String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getWalletFingerPrint() {
        return walletFingerPrint;
    }

    public void setWalletFingerPrint(String fingerPrint) {
        this.walletFingerPrint = fingerPrint;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getExPubPath() {
        return exPubPath;
    }

    public void setExPubPath(String expubPath) {
        this.exPubPath = expubPath;
    }

    public String getExPubs() {
        return exPubs;
    }

    public void setExPubs(String xPubs) {
        this.exPubs = xPubs;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String deriveAddress(int[] index) {
        Deriver deriver = new Deriver(true);
        List<String> xpubList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(getExPubs());
            for (int i = 0; i < jsonArray.length(); i++) {
                xpubList.add(jsonArray.getJSONObject(i).getString("xpub"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String address = deriver.deriveMultiSigAddress(getThreshold(),
                xpubList, new int[] {index[0], index[1]},
                MultiSig.Account.ofPath(getExPubPath()));
        return address;
    }

    @Override
    public String toString() {
        return "MultiSigWalletEntity{" +
                "WalletFingerPrint=" + walletFingerPrint +
                ", walletName='" + walletName + '\'' +
                ", threshold=" + threshold +
                ", total=" + total +
                ", exPubPath='" + exPubPath + '\'' +
                ", exPubs='" + exPubs + '\'' +
                ", belongTo='" + belongTo + '\'' +
                ", network='" + network + '\'' +
                '}';
    }
}
