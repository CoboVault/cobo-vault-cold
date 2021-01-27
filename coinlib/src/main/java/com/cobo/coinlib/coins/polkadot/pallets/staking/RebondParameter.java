package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class RebondParameter  extends Parameter {
    private final BigInteger value;
    public RebondParameter(String name, Network network, int code, BigInteger value) {
        super(name, network, code);
        this.value = value;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Value", Utils.getReadableBalanceString(network, value));
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeBIntCompact(value);
    }
}
