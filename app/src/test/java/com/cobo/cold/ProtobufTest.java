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

package com.cobo.cold;

import com.cobo.bcUniformResource.UniformResource;
import com.cobo.cold.protobuf.BaseProtoc;
import com.cobo.cold.protobuf.MessageProtoc;
import com.cobo.cold.protobuf.PayloadProtoc;
import com.cobo.cold.protobuf.SyncProtoc;

import org.junit.Test;
import org.spongycastle.util.encoders.Base64;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ProtobufTest {

    @Test
    public void testSync() {
        SyncProtoc.Account.Builder zcoinAccount = SyncProtoc.Account.newBuilder()
                .setHdPath("M/44'/136'/0'")
                .setXPub("xpub6CCyKnUGB4VjMLZYGaSvBijULh1Hd2Uimy9EKy5yUQt9Yexb7s24We2CTM54hWaQZYhCzSR6yEFAs5cQ8TwbaSn53S6HRrmaFkdgqczb85v")
                .setAddressLength(1)
                .setIsMultiSign(false);

        SyncProtoc.Coin.Builder zcoin = SyncProtoc.Coin.newBuilder()
                .setCoinCode("ZCOIN")
                .setActive(true)
                .addAccounts(zcoinAccount);


        SyncProtoc.Account.Builder dashAccount = SyncProtoc.Account.newBuilder()
                .setHdPath("M/44'/5'/0'")
                .setXPub("xpub6CYEjsU6zPM3sADS2ubu2aZeGxCm3C5KabkCpo4rkNbXGAH9M7rRUJ4E5CKiyUddmRzrSCopPzisTBrXkfCD4o577XKM9mzyZtP1Xdbizyk")
                .setAddressLength(1)
                .setIsMultiSign(false);

        SyncProtoc.Coin.Builder dash = SyncProtoc.Coin.newBuilder()
                .setCoinCode("DASH")
                .setActive(true)
                .addAccounts(dashAccount);

        SyncProtoc.Sync.Builder sync = SyncProtoc.Sync.newBuilder()
                .addCoins(dash)
                .addCoins(zcoin);


        PayloadProtoc.Payload.Builder payload = PayloadProtoc.Payload.newBuilder()
                .setType(PayloadProtoc.Payload.Type.TYPE_SYNC)
                .setUuid("464ce7dec19b055796f5a686630d477b06ed1eb1ad579d237741271a5729e241d1bd7e0f613579dcbbd298241551aeb3196c26f085f33d191a55da2ef6fc65d0e7cbe7eec2175add221e758cfa931eb6")
                .setSync(sync);

        BaseProtoc.Base.Builder base = BaseProtoc.Base.newBuilder()
                .setData(payload)
                .setVersion(1)
                .setDescription("cobo valut qrcode protocol")
                .setColdVersion(10226);

        byte[] data = base.build().toByteArray();
        assertEquals("CAESGmNvYm8gdmFsdXQgcXJjb2RlIHByb3RvY29sGscDCAESoAE0NjRjZTdkZWMxOWIwNTU3OTZmNWE2ODY2MzBkNDc3YjA2ZWQxZWIxYWQ1NzlkMjM3NzQxMjcxYTU3MjllMjQxZDFiZDdlMGY2MTM1NzlkY2JiZDI5ODI0MTU1MWFlYjMxOTZjMjZmMDg1ZjMzZDE5MWE1NWRhMmVmNmZjNjVkMGU3Y2JlN2VlYzIxNzVhZGQyMjFlNzU4Y2ZhOTMxZWI2Gp8CCosBCgREQVNIEAEagAEKC00vNDQnLzUnLzAnEm94cHViNkNZRWpzVTZ6UE0zc0FEUzJ1YnUyYVplR3hDbTNDNUthYmtDcG80cmtOYlhHQUg5TTdyUlVKNEU1Q0tpeVVkZG1SenJTQ29wUHppc1RCclhrZkNENG81NzdYS005bXp5WnRQMVhkYml6eWsYAQqOAQoFWkNPSU4QARqCAQoNTS80NCcvMTM2Jy8wJxJveHB1YjZDQ3lLblVHQjRWak1MWllHYVN2QmlqVUxoMUhkMlVpbXk5RUt5NXlVUXQ5WWV4YjdzMjRXZTJDVE01NGhXYVFaWWhDelNSNnlFRkFzNWNROFR3YmFTbjUzUzZIUnJtYUZrZGdxY3piODV2GAEo8k8=", Base64.toBase64String(data));

    }

    @Test
    public void testMessage() {
        MessageProtoc.SignMessage.Builder builder = MessageProtoc.SignMessage.newBuilder()
                .setCoinCode("BTC").setHdPath("M/49'/0/'0'/0/0").setMessage("hello");

        PayloadProtoc.Payload.Builder payload = PayloadProtoc.Payload.newBuilder()
                .setType(PayloadProtoc.Payload.Type.TYPE_SIGN_MSG)
                .setUuid("464ce7dec19b055796f5a686630d477b06ed1eb1ad579d237741271a5729e241d1bd7e0f613579dcbbd298241551aeb3196c26f085f33d191a55da2ef6fc65d0e7cbe7eec2175add221e758cfa931eb6")
                .setSignMsg(builder);

        BaseProtoc.Base.Builder base = BaseProtoc.Base.newBuilder()
                .setData(payload)
                .setVersion(1)
                .setDescription("cobo valut qrcode protocol")
                .setColdVersion(10226);

        assertEquals("CAESGmNvYm8gdmFsdXQgcXJjb2RlIHByb3RvY29sGsQBCAMSoAE0NjRjZTdkZWMxOWIwNTU3OTZmNWE2ODY2MzBkNDc3YjA2ZWQxZWIxYWQ1NzlkMjM3NzQxMjcxYTU3MjllMjQxZDFiZDdlMGY2MTM1NzlkY2JiZDI5ODI0MTU1MWFlYjMxOTZjMjZmMDg1ZjMzZDE5MWE1NWRhMmVmNmZjNjVkMGU3Y2JlN2VlYzIxNzVhZGQyMjFlNzU4Y2ZhOTMxZWI2Kh0KA0JUQxIPTS80OScvMC8nMCcvMC8wGgVoZWxsbyjyTw==",
                Base64.toBase64String(base.build().toByteArray()));

    }

    @Test
    public void test() throws Exception {
        String s = UniformResource.Decoder.decode(new String[] {"UR:BYTES/TZMPYQQQY2QQQQQQYSQTSEYZV9QQQQQQP04UYQRGGQQQQQQQQQQQCUEPQV55UAN06KZVJ5UFZXPGA6VPCJNNMC8244D0PRPH0P5U8PRH6FSC7AZXXPZQYGRXAQ45WLCRP9UU0LJA85UZW627XSLY2HVKA4MUYK82FAMMQCAG2CPZQ7VQP25MHGZES5QPWZYNJXE64PE9Y4QSV5WMQ4Y5EDUU4PCZ9QDQSY2R94Y6Q6NM6HKSRHGF38NC84ZP6908NZYGX9X63FQ78CDAGWYCK0XRGAJCMHL62RR7W2CZCPS9A".toLowerCase()});
        System.out.println(s);
    }

}
