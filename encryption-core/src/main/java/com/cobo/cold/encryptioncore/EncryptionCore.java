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

package com.cobo.cold.encryptioncore;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.cobo.cold.encryptioncore.base.Config;
import com.cobo.cold.encryptioncore.base.Packet;
import com.cobo.cold.encryptioncore.cipher.CipherImpl;
import com.cobo.cold.encryptioncore.interfaces.Callback;
import com.cobo.cold.encryptioncore.interfaces.Cipher;
import com.cobo.cold.encryptioncore.interfaces.JobScheduler;
import com.cobo.cold.encryptioncore.interfaces.SerialManagerProxy;
import com.cobo.cold.encryptioncore.job.JobSchedulerImpl;
import com.cobo.cold.encryptioncore.serial.SerialManagerProxyImpl;
import com.cobo.cold.encryptioncore.utils.Preconditions;

public class EncryptionCore implements JobScheduler {
    private static EncryptionCore sInstance;
    private final JobScheduler mImpl;

    private EncryptionCore(@NonNull SerialManagerProxy serialManager, @NonNull Config config) {
        Preconditions.checkNotNull(serialManager);
        mImpl = new JobSchedulerImpl(serialManager, getCipher(config.secretKey));
    }

    @NonNull
    public static EncryptionCore getInstance() {
        return Preconditions.checkNotNull(sInstance, "initialize first");
    }

    public static void initialize(@NonNull Context context, @NonNull Config config) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(config);
        Preconditions.checkState(Looper.myLooper() == Looper.getMainLooper(), "initialize in main thread only");

        sInstance = new EncryptionCore(SerialManagerProxyImpl.newInstance(context, config.portSpeed), config);
    }

    private Cipher getCipher(@Nullable Pair<byte[], byte[]> secretKey) {
        final Cipher impl;

        if (secretKey == null) {
            impl = null;
        } else {
            Preconditions.checkArgument(secretKey.first != null && secretKey.second != null,
                    "des algorithm need key and key iv");
            impl = new CipherImpl(secretKey.first, secretKey.second);
        }

        return impl;
    }

    @Override
    public void offer(@NonNull Packet packet, @NonNull Callback callback) {
        Preconditions.checkNotNull(packet);
        Preconditions.checkNotNull(callback);

        mImpl.offer(packet, callback);
    }
}
