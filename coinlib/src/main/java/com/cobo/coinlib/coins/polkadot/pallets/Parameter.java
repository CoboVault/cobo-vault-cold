package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Parameter {
    protected Network network;
    public String name;
    public int code;

    public Parameter(Network network, String name, int code) {
        this.network = network;
        this.name = name;
        this.code = code;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("chain", network.name);
        return object;
    }

    public void writeTo(ScaleCodecWriter scw) throws IOException {
        scw.writeByte((this.code >> 8) & 0xff);
        scw.writeByte(this.code & 0xff);
    }
}
