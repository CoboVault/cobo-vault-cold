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
    public SetKeysParameter(Network network, String name, int code, List<byte[]> publicKeys, byte[] proof) {
        super(network, name, code);
        this.proof = proof;
        this.publicKeys = publicKeys;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("rotateKeys", publicKeys.stream().map(p -> Hex.toHexString(p)).collect(Collectors.toList()));
        object.put("proof", Hex.toHexString(proof));
        return object;
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
