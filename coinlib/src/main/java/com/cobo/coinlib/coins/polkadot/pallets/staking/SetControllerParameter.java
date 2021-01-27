package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SetControllerParameter extends Parameter {
    private byte[] publicKey;

    public SetControllerParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        publicKey = scr.readByteArray(32);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Controller", AddressCodec.encodeAddress(publicKey, network.SS58Prefix));
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeByteArray(publicKey);
    }
}
