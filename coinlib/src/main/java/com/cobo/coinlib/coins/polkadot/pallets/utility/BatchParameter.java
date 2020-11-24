package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class BatchParameter extends Parameter {
    private final int length;
    private final List<Parameter> parameters;

    public BatchParameter(Network network, String name, int length, List<Parameter> parameters) {
        super(network, name);
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
}
