package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NominateParameter extends Parameter {
    private int length;
    private List<byte[]> publicKeys;

    public NominateParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        publicKeys = new ArrayList<>();
        length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            publicKeys.add(scr.readByteArray(32));
        }
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Length", length);
        object.put("Targets", toJsonArray());
        return object;
    }

    public JSONArray toJsonArray() {
        JSONArray array = new JSONArray();
        for (byte[] pubkey : publicKeys) {
            array.put(AddressCodec.encodeAddress(pubkey, network.SS58Prefix));
        }
        return array;
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeCompact(length);
        for (byte[] pk : publicKeys) {
            scw.writeByteArray(pk);
        }
    }
}
