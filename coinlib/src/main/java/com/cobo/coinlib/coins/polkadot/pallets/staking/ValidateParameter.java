package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class ValidateParameter extends Parameter {
    private int value; // base 1 * 10^9

    public ValidateParameter(Network network, String name, int value) {
        super(network, name);
        this.value = value;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("value", BigDecimal.valueOf(value).divide(BigDecimal.TEN.pow(9)));
        return object;
    }
}
