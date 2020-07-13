package com.cobo.cold.db.entity;


import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "multi_sig_wallet", indices = {@Index("walletId")})
public class MultiSigWalletEntity {
    @PrimaryKey(autoGenerate = true)
    private long walletId;
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

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
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
}
