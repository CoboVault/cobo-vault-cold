package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class WithdrawUnbondedParameter extends Parameter {
    private long numSlashingSpans;

    public WithdrawUnbondedParameter(String name, Network network, int code, long numSlashingSpans) {
        super(name, network, code);
        this.numSlashingSpans = numSlashingSpans;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        JSONObject parameter = new JSONObject();
        parameter.put("numSlashingSpans", numSlashingSpans);
        object.put("parameter", parameter);
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeUint32(numSlashingSpans);
    }
}
