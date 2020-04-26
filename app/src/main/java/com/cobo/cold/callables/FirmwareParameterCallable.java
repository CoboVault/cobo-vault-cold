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
import com.cobo.cold.encryptioncore.base.Payload;

import java.util.concurrent.Callable;

public class FirmwareParameterCallable implements Callable<String[]> {
    @Override
    public String[] call() {
        try {
            String[] res = new String[2];
            final Callable<Packet> callable = new BlockingCallable(
                    new Packet.Builder(CONSTANTS.METHODS.GET_FIRMWARE_PARAMETER).build());
            final Packet result = callable.call();
            Payload payload = result.getPayload(CONSTANTS.TAGS.FIRMWARE_SN);
            if (payload != null) {
                res[0] = payload.toUtf8();
            }
            payload = result.getPayload(CONSTANTS.TAGS.FIRMWARE_APP_VERSION);
            if (payload != null) {
                res[1] = payload.toUtf8();
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
