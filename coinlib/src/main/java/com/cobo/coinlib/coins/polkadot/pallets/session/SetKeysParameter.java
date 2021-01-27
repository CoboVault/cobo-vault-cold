package com.cobo.coinlib.coins.polkadot.pallets.session;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SetKeysParameter extends Parameter {
    private List<byte[]> publicKeys;
    private byte[] proof;

    public SetKeysParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        publicKeys = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            publicKeys.add(scr.readByteArray(32));
        }
        proof = scr.readByteArray(1);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("Keys", publicKeys.stream().map(Hex::toHexString).collect(Collectors.toList()))
                .put("Proof", Hex.toHexString(proof));
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        for (byte[] pk : publicKeys) {
            scw.writeByteArray(pk);
        }
        scw.writeByteArray(proof);
    }
}
