/*
 *
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
 *
 */

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

    @Query("SELECT * FROM multi_sig_address where walletFingerPrint=:walletFingerPrint AND path=:path")
    MultiSigAddressEntity loadAddressByPath(String walletFingerPrint, String path);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<MultiSigAddressEntity> addressEntities);

    @Query("SELECT * FROM multi_sig_address")
    LiveData<List<MultiSigAddressEntity>> loadAll();
}
