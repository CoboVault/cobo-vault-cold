package com.cobo.cold.db.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "multi_sig_wallet", indices = {@Index("walletFingerPrint")})
public class MultiSigWalletEntity {
    @PrimaryKey
    @NonNull
    private String walletFingerPrint;
    private String walletName;
    private int threshold;
    private int total;
    private String exPubPath;
    private String exPubs;
    private String belongTo;
    private String network;

    public MultiSigWalletEntity(String walletName, int threshold, int total, String exPubPath, String exPubs, String belongTo, String network) {
        this.walletName = walletName;
        this.threshold = threshold;
        this.total = total;
        this.exPubPath = exPubPath;
        this.exPubs = exPubs;
        this.belongTo = belongTo;
        this.network = network;
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
