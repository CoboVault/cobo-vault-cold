package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public abstract class Parameter {
    protected Network network;
    public String name;
    public int code;

    public Parameter(String name, Network network,  int code) {
        this.network = network;
        this.name = name;
        this.code = code;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("chain", network.name);
        object.put("parameter", addCallParameter());
        return object;
    }

    protected abstract JSONObject addCallParameter() throws JSONException;

    public void writeTo(ScaleCodecWriter scw) throws IOException {
        scw.writeByte((this.code >> 8) & 0xff);
        scw.writeByte(this.code & 0xff);
    }
}