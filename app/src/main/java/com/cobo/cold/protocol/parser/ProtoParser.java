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

package com.cobo.cold.protocol.parser;

import com.cobo.cold.encryptioncore.utils.Preconditions;
import com.cobo.cold.protobuf.BaseProtoc.Base;
import com.cobo.cold.protobuf.PayloadProtoc.Payload;
import com.cobo.cold.protobuf.SyncProtoc;
import com.cobo.cold.protocol.ZipUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Base64;

public class ProtoParser {

    private final byte[] protoBytes;
    private Payload payload;
    private Payload.Type type;

    public ProtoParser(String str, boolean compress) {
        byte[] buffer = Base64.decode(str);
        if (compress) {
            buffer = ZipUtil.unzip(buffer);
        }
        protoBytes = buffer;
    }

    public ProtoParser(byte[] bytes) {
        protoBytes = bytes;
    }

    public JSONObject parseToJson() {
        try {
            Base base = Base.parseFrom(protoBytes);
            payload = base.getData();
            return new JSONObject(new JsonFormat().printToString(payload));
        } catch (InvalidProtocolBufferException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SyncProtoc.Sync getSync() {
        Preconditions.checkArgument(type == Payload.Type.TYPE_SYNC, "is not sync type");
        return payload.getSync();
    }

}
