package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class UnlockParameter extends Parameter {
    private byte[] target;

    public UnlockParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(target);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        target = scr.readByteArray(32);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Target", AddressCodec.encodeAddress(target, network.SS58Prefix));
    }
}
