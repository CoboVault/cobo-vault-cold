package com.cobo.coinlib.coins.polkadot.pallets.session;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SetKeysParameter extends Parameter {
    private final List<byte[]> publicKeys;
    private final byte[] proof;

    public SetKeysParameter(String name, Network network, int code, List<byte[]> publicKeys, byte[] proof) {
        super(name, network, code);
        this.proof = proof;
        this.publicKeys = publicKeys;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("keys", publicKeys.stream().map(p -> Hex.toHexString(p)).collect(Collectors.toList()))
                .put("proof", Hex.toHexString(proof));
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        for (byte[] pk: publicKeys) {
            scw.writeByteArray(pk);
        }
        scw.writeByteArray(proof);
    }
}
