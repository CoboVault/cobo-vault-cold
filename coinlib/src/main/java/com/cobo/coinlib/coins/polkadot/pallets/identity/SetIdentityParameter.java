package com.cobo.coinlib.coins.polkadot.pallets.identity;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetIdentityParameter extends Parameter {
    private Map<byte[], byte[]> additional = new HashMap<>();
    private byte[] display;
    private byte[] legal;
    private byte[] web;
    private byte[] riot;
    private byte[] email;
    private byte[] pgpFingerprint;
    private byte[] image;
    private byte[] twitter;

    public SetIdentityParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        writeAdditional(scw);
        writeRaw(scw, display);
        writeRaw(scw, legal);
        writeRaw(scw, web);
        writeRaw(scw, riot);
        writeRaw(scw, email);
        writePgpFingerprint(scw);
        writeRaw(scw, image);
        writeRaw(scw, twitter);
    }

    private byte[] readRaw(ScaleCodecReader scr) {
        int rawLength = scr.readByte();
        byte[] content = new byte[0];
        if(rawLength > 1 ){
            content = scr.readByteArray(rawLength - 1);
        }
        return content;
    }

    private void writeRaw(ScaleCodecWriter scw, byte[] data) throws IOException {
        int length = data.length;
        scw.writeByte(length);
        scw.writeByteArray(data);
    }

    private void readAdditional(ScaleCodecReader scr) {
        int length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            byte[] key = readRaw(scr);
            byte[] content = readRaw(scr);
            additional.put(key, content);
        }
    }

    private void writeAdditional(ScaleCodecWriter scw) throws IOException {
        int size = additional.size();
        scw.writeCompact(size);
        additional.forEach((key, value) -> {
            try {
                writeRaw(scw, key);
                writeRaw(scw, value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void readPgpFingerprint(ScaleCodecReader scr) {
        boolean isSome = scr.readBoolean();
        if (isSome) {
            pgpFingerprint = scr.readByteArray(20);
        }
    }

    private void writePgpFingerprint(ScaleCodecWriter scw) throws IOException {
        if (pgpFingerprint.length > 0) {
            scw.writeByte(0x01);
            scw.writeByteArray(pgpFingerprint);
        }
        else {
            scw.writeByte(0x00);
        }
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        readAdditional(scr);
        display = readRaw(scr);
        legal = readRaw(scr);
        web = readRaw(scr);
        riot = readRaw(scr);
        email = readRaw(scr);
        readPgpFingerprint(scr);
        image = readRaw(scr);
        twitter = readRaw(scr);
    }

    private String fromHexToUtf8String(byte[] hex) {
        return new String(hex, StandardCharsets.UTF_8);
    }

    private JSONObject getAdditional() {
        JSONObject object = new JSONObject();
        additional.forEach((key, value) -> {
            try {
                object.put(fromHexToUtf8String(key), fromHexToUtf8String(value));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        return object;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Additional", getAdditional())
                .put("Display", fromHexToUtf8String(display))
                .put("Legal", fromHexToUtf8String(legal))
                .put("Web", fromHexToUtf8String(web))
                .put("Riot", fromHexToUtf8String(riot))
                .put("Email", fromHexToUtf8String(email))
                .put("PgpFingerprint", Hex.toHexString(pgpFingerprint))
                .put("Image", fromHexToUtf8String(image))
                .put("Twitter", fromHexToUtf8String(twitter));
        return object;
    }
}
