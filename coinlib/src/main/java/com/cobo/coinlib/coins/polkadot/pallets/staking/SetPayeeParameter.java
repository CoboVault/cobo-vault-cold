package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class SetPayeeParameter extends Parameter {
    private final Payee payee;
    public SetPayeeParameter(String name, Network network, int code, Payee payee) {
        super(name, network, code);
        this.payee = payee;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        payee.writeToJSON(network, object);
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        payee.writeTo(scw);
    }
}
