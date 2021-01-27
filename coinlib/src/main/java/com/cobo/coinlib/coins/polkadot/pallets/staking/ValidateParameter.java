package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;

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
        object.put("Prefs", BigDecimal.valueOf(value).divide(BigDecimal.TEN.pow(9)));
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeCompact(value);
    }
}
