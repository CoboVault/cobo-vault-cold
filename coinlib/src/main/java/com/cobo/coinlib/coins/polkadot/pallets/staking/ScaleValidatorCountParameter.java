package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ScaleValidatorCountParameter extends Parameter {
    // TODO: need to check with true extrinsic
    private long percent; // base 100
    public ScaleValidatorCountParameter(String name, Network network, int code, long percent) {
        super(name, network, code);
        this.percent = percent;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Factor", percent);
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeUint32(percent);
    }
}
