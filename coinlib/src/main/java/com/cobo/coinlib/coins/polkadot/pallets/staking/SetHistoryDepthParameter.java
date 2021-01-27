package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
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

    public SetHistoryDepthParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        eraIndex = scr.readCompact();
        _eraItemsDeleted = scr.readCompact();
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
