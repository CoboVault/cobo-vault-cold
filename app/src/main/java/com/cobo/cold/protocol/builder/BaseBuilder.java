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

package com.cobo.cold.protocol.builder;

import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.cobo.cold.BuildConfig;
import com.cobo.cold.callables.GetUuidCallable;
import com.cobo.cold.encryptioncore.utils.ByteFormatter;
import com.cobo.cold.protobuf.BaseProtoc;
import com.cobo.cold.protobuf.PayloadProtoc;
import com.cobo.cold.protobuf.SignTransactionResultProtoc;
import com.cobo.cold.protobuf.SyncProtoc;
import com.cobo.cold.protocol.EncodeConfig;
import com.cobo.cold.protocol.ZipUtil;
import com.googlecode.protobuf.format.JsonFormat;

import org.spongycastle.util.encoders.Base64;

public class BaseBuilder {

    protected final BaseProtoc.Base.Builder base;
    protected PayloadProtoc.Payload.Builder payload;
    protected SyncProtoc.Sync.Builder sync;
    protected SignTransactionResultProtoc.SignTransactionResult.Builder signTxResult;
    private final EncodeConfig config;

    BaseBuilder(PayloadProtoc.Payload.Type type, EncodeConfig config) {
        Header header = new Header();
        base = BaseProtoc.Base.newBuilder()
                .setVersion(header.version)
                .setDescription(header.description)
                .setDeviceType(header.deviceType)
                .setColdVersion(header.coldVersion);
        initPayload(type, header);
        this.config = config;
    }

    private void initPayload(PayloadProtoc.Payload.Type type, Header header) {
        payload = PayloadProtoc.Payload.newBuilder()
                .setUuid(header.uuid);

        switch (type) {
            case TYPE_SYNC:
                sync = SyncProtoc.Sync.newBuilder();
                break;
            case TYPE_SIGN_TX_RESULT:
                signTxResult = SignTransactionResultProtoc.SignTransactionResult.newBuilder();
                break;
        }
        payload.setType(type);
    }

    public String build() {
        base.setData(payload);
        if (BuildConfig.DEBUG) {
            String json = new JsonFormat().printToString(base.build());
            String TAG = "Vault.QrCode";
            Log.d(TAG, "json = " + json);
        }
        byte[] data = getBytes();
        return getEncodedString(data);
    }

    private String getEncodedString(byte[] data) {
        String res;
        switch (config.encoding) {
            case Hex:
                res = ByteFormatter.bytes2hex(data);
                break;
            case BASE64:
            default:
                res = Base64.toBase64String(data);
        }
        return res;
    }

    private byte[] getBytes() {
        byte[] data;
        switch (config.format) {
            case JSON:
                data = new JsonFormat().printToString(base.build()).getBytes();
                break;
            case PROTOBUF:
            default:
                data = base.build().toByteArray();
        }

        data = config.compress ? ZipUtil.zip(data) : data;
        return data;
    }

    class Header {
        private final int version = 1;
        private final String uuid;
        private final String description;
        private final int coldVersion;
        private final String deviceType;

        Header() {
            String uuid = getUuid();
            this.uuid = TextUtils.isEmpty(uuid) ? " " : uuid;
            description = "cobo vault qrcode";
            coldVersion = BuildConfig.VERSION_CODE;
            deviceType = getDeviceType();
        }

        private String getDeviceType() {
            String boardType = SystemProperties.get("boardtype");
            if ("B".equals(boardType)) {
                return "Cobo Vault Essential";
            } else {
                return "Cobo Vault Pro";
            }
        }

        private String getUuid() {
            return new GetUuidCallable().call();
        }
    }
}
