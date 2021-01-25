package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class NominateParameter extends Parameter {
    private final int length;
    private final List<byte[]> publicKeys;

    public NominateParameter(String name, Network network, int code, int length, List<byte[]> publicKeys) {
        super(name, network, code);
        this.length = length;
        this.publicKeys = publicKeys;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("length", length);
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
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeCompact(length);
        for (byte[] pk :
                publicKeys) {
            scw.writeByteArray(pk);
        }
    }
}
