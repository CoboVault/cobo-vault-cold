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

import android.text.TextUtils;

import com.cobo.coinlib.Util;
import com.cobo.cold.encryption.interfaces.CONSTANTS;
import com.cobo.cold.encryptioncore.base.Packet;
import com.cobo.cold.encryptioncore.base.Payload;

import java.util.concurrent.Callable;

public class GetVaultIdCallable implements Callable<String> {
    private static final String pubKeyPath = "M/44'/1131373167'/0'";

    @Override
    public String call() {
        final Callable<Packet> callable = new BlockingCallable(
                new Packet.Builder(CONSTANTS.METHODS.GET_EXTENDED_PUBLICKEY)
                        .addTextPayload(CONSTANTS.TAGS.PATH, pubKeyPath).build());
        final Packet result;
        String xPubKey = null;
        try {
            result = callable.call();
            final Payload payload = result.getPayload(CONSTANTS.TAGS.EXTEND_PUB_KEY);
            if (payload != null) {
                xPubKey = payload.toUtf8();
            }
            String pubKey;
            if (!TextUtils.isEmpty(xPubKey)) {
                pubKey = Util.pubKeyFromExtentPubKey(xPubKey);
                return pubKey.substring(2, 8).toUpperCase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}