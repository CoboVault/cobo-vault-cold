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

package com.cobo.cold.callables;

import androidx.annotation.NonNull;

import com.cobo.coinlib.MnemonicUtils;
import com.cobo.cold.encryption.interfaces.CONSTANTS;
import com.cobo.cold.encryptioncore.base.Packet;

import java.util.concurrent.Callable;

public class WriteMnemonicCallable implements Callable<Boolean> {

    private final String mnemonic;
    private final String password;

    public WriteMnemonicCallable(@NonNull String mnemonic, @NonNull String password) {
        this.mnemonic = mnemonic;
        this.password = password;
    }

    @Override
    public Boolean call() {
        try {
            final Packet packet = new Packet.Builder(CONSTANTS.METHODS.WRITE_MNEMONIC)
                    .addTextPayload(CONSTANTS.TAGS.MNEMONIC, mnemonic)
                    .addBytesPayload(CONSTANTS.TAGS.ENTROPY, MnemonicUtils.generateEntropy(mnemonic))
                    .addHexPayload(CONSTANTS.TAGS.CURRENT_PASSWORD, password)
                    .build();
            final Callable<Packet> callable = new BlockingCallable(packet);
            callable.call();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}