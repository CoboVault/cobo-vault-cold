package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ValidateParameter extends Parameter {
    private int value; // base 1 * 10^9

    public ValidateParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        this.value = scr.readCompactInt();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Prefs", value / 10000000);
        return object;
    }

    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeCompact(value);
    }
}
