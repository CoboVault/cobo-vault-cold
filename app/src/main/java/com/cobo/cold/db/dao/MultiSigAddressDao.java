package com.cobo.cold.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cobo.cold.db.entity.MultiSigAddressEntity;

import java.util.List;

@Dao
public interface MultiSigAddressDao {
    @Query("SELECT * FROM multi_sig_address where walletFingerPrint=:walletFingerPrint")
    List<MultiSigAddressEntity> loadAllMultiSigAddressSync(String walletFingerPrint);

    @Query("SELECT * FROM multi_sig_address where walletFingerPrint=:walletFingerPrint")
    LiveData<List<MultiSigAddressEntity>> loadAllMultiSigAddress(String walletFingerPrint);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<MultiSigAddressEntity> addressEntities);

    @Query("SELECT * FROM multi_sig_address")
    LiveData<List<MultiSigAddressEntity>> loadAll();
}
