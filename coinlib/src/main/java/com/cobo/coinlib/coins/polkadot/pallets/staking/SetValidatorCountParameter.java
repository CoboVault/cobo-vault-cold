package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SetValidatorCountParameter extends Parameter {
    private final long newValue;
    public SetValidatorCountParameter(String name, Network network, int code, long newValue) {
        super(name, network, code);
        this.newValue = newValue;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("new", newValue);
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeLIntCompact(newValue);
    }
}
