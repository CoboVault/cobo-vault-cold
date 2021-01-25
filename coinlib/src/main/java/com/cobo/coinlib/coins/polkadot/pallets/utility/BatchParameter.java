package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class BatchParameter extends Parameter {
    private final int length;
    private final List<Parameter> parameters;

    public BatchParameter(Network network, String name, int code, int length, List<Parameter> parameters) {
        super(name, network,  code);
        this.length = length;
        this.parameters = parameters;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Length", length);
        object.put("Pallets", toJsonArray(parameters));
        return object;
    }

    public JSONArray toJsonArray(List<Parameter> parameters) throws JSONException {
        JSONArray array = new JSONArray();
        for (Parameter parameter: parameters) {
            array.put(parameter.toJSON());
        }
        return array;
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
