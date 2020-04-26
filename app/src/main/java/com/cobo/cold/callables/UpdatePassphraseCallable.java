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

import com.cobo.cold.encryption.interfaces.CONSTANTS;
import com.cobo.cold.encryptioncore.base.Packet;

import java.util.concurrent.Callable;

public class UpdatePassphraseCallable implements Callable<String> {

    private final String passphrase;
    private final String password;

    public UpdatePassphraseCallable(String passphrase, String password) {
        this.passphrase = passphrase;
        this.password = password;
    }

    @Override
    public String call() {
        try {
            final Packet packet = new Packet.Builder(CONSTANTS.METHODS.UPDATE_PASSPHRASE)
                    .addTextPayload(CONSTANTS.TAGS.PASSPHRASE, passphrase)
                    .addHexPayload(CONSTANTS.TAGS.CURRENT_PASSWORD, password)
                    .build();
            final Callable<Packet> callable = new BlockingCallable(packet);
            callable.call();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}