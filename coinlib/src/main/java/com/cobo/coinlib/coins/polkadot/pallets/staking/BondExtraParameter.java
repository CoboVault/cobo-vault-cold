package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class BondExtraParameter extends Parameter {
    private final BigInteger additionalAmount;

    public BondExtraParameter(Network network, String name, int code, BigInteger additionalAmount) {
        super(network, name, code);
        this.additionalAmount = additionalAmount;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("MaxAdditional", Utils.getReadableBalanceString(this.network, this.additionalAmount));
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeBIntCompact(additionalAmount);
    }
}
