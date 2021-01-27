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
        super(name, network, code);
        this.additionalAmount = additionalAmount;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("MaxAdditional", Utils.getReadableBalanceString(this.network, this.additionalAmount));
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeBIntCompact(additionalAmount);
    }
}
