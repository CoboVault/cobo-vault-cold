package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class BatchParameter extends Parameter {
    private final int length;
    private final List<Parameter> parameters;

    public BatchParameter(Network network, String name, int code, int length, List<Parameter> parameters) {
        super(network, name, code);
        this.length = length;
        this.parameters = parameters;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("length", length);
        object.put("pallets", parameters);
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeCompact(length);
        for (Parameter p:
             parameters) {
            p.writeTo(scw);
        }
    }
}
