package com.cobo.cold.db.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "multi_sig_address",
        foreignKeys = @ForeignKey(entity = MultiSigWalletEntity.class,
                parentColumns = "walletFingerPrint",
                childColumns = "walletFingerPrint", onDelete = CASCADE),
        indices = {@Index(value = "id",unique = true), @Index(value = "walletFingerPrint")})
public class MultiSigAddressEntity {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public long id;
    @NonNull
    private String address; // address
    @NonNull
    private int index; // address index
    @NonNull
    private String walletFingerPrint; // belong to which multisig wallet
    @NonNull
    private String path; // address path
    @NonNull
    private int changeIndex;
    private String name;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getWalletFingerPrint() {
        return walletFingerPrint;
    }

    public void setWalletFingerPrint(String walletFingerPrint) {
        this.walletFingerPrint = walletFingerPrint;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MultiSigAddressEntity{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", index=" + index +
                ", walletFingerPrint=" + walletFingerPrint +
                ", path='" + path + '\'' +
                ", changeIndex=" + changeIndex +
                ", name='" + name + '\'' +
                '}';
    }
}
