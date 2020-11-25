package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.UOS.Network;

import org.json.JSONException;
import org.json.JSONObject;

public class Parameter {
    protected Network network;
    public String name;

    public Parameter(Network network, String name) {
        this.network = network;
        this.name = name;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("chain", network.name);
        return object;
    }
}
