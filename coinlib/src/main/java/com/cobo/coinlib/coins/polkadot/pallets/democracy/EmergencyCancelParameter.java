package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class EmergencyCancelParameter extends Parameter {
    private long refIndex;
    public EmergencyCancelParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeUint32(refIndex);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        refIndex = scr.readUint32();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("RefIndex", refIndex);
    }
}