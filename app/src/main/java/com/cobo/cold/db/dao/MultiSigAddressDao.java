package com.cobo.cold.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.cobo.cold.db.entity.MultiSigAddressEntity;

import java.util.List;

@Dao
public interface MultiSigAddressDao {
    @Query("SELECT * FROM multi_sig_address where walletId=:walletId")
    LiveData<List<MultiSigAddressEntity>> loadAllMultiSigAddress(long walletId);
}
