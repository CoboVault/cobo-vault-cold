package com.cobo.cold.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cobo.cold.db.entity.MultiSigAddressEntity;

import java.util.List;

@Dao
public interface MultiSigAddressDao {
    @Query("SELECT * FROM multi_sig_address where walletId=:walletId")
    List<MultiSigAddressEntity> loadAllMultiSigAddress(long walletId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<MultiSigAddressEntity> addressEntities);
}
