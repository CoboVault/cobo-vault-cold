package com.cobo.cold.db.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "multi_sig_address",
        foreignKeys = @ForeignKey(entity = MultiSigWalletEntity.class,
                parentColumns = "walletId",
                childColumns = "walletId", onDelete = CASCADE),
        indices = {@Index(value = "id",unique = true),@Index(value = "walletId")})
public class MultiSigAddressEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    private String address; // address
    private String index; // address index
    private String walletId; // belong to which multisig wallet
    private String path; // address path
    private int changeIndex;

    public MultiSigAddressEntity(long id, String address, String index, String walletId, String path, int changeIndex) {
        this.id = id;
        this.address = address;
        this.index = index;
        this.walletId = walletId;
        this.path = path;
        this.changeIndex = changeIndex;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getChangeIndex() {
        return changeIndex;
    }

    public void setChangeIndex(int changeIndex) {
        this.changeIndex = changeIndex;
    }
}
