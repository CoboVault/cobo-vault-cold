package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class SetHistoryDepthParameter extends Parameter {
    private BigInteger eraIndex;
    private BigInteger _eraItemsDeleted;

    public SetHistoryDepthParameter(String name, Network network, int code, BigInteger eraIndex, BigInteger _eraItemsDeleted) {
        super(name, network, code);
        this.eraIndex = eraIndex;
        this._eraItemsDeleted = _eraItemsDeleted;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return null;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeBIntCompact(eraIndex);
        scw.writeBIntCompact(_eraItemsDeleted);
    }
}
