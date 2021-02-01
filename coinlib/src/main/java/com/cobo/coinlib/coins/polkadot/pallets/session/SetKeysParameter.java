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
import java.util.stream.IntStream;

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
        String[] keyNames = new String[] {
                "authority_discovery",
                "babe",
                "grandpa",
                "im_online",
                "parachains"
        };
        return new JSONObject()
                .put("Keys", IntStream.range(0, keyNames.length)
                        .mapToObj(i -> keyNames[i] + ":\n" + "0x" + Hex.toHexString(publicKeys.get(i)) + "\n")
                        .reduce((s1, s2) -> s1 + s2)
                        .orElse(""))
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
