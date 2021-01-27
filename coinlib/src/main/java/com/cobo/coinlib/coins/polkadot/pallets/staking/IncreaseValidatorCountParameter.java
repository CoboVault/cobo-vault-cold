package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class IncreaseValidatorCountParameter extends Parameter {
    private long additional;

    public IncreaseValidatorCountParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        additional = scr.readCompactInt();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("additional", additional);
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeLIntCompact(additional);
    }
}
