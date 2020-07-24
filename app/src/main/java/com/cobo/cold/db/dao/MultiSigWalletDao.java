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

package com.cobo.cold.db.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.cobo.cold.db.entity.MultiSigWalletEntity;

import java.util.List;

@Dao
public interface MultiSigWalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(MultiSigWalletEntity wallet);

    @Query("SELECT * FROM multi_sig_wallet WHERE belongTo=:xfp")
    LiveData<List<MultiSigWalletEntity>> loadAll(String xfp);

    @Query("SELECT * FROM multi_sig_wallet WHERE belongTo=:xfp")
    List<MultiSigWalletEntity> loadAllSync(String xfp);

    @Update
    int update(MultiSigWalletEntity walletEntity);

    @Query("DELETE FROM multi_sig_wallet WHERE walletFingerPrint=:walletFingerPrint")
    int delete(String walletFingerPrint);

    @Query("SELECT * FROM multi_sig_wallet WHERE walletFingerPrint=:walletFingerPrint")
    MultiSigWalletEntity loadWallet(String walletFingerPrint);
}
