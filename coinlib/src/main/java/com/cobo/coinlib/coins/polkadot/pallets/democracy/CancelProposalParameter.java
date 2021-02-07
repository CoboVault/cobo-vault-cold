package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class CancelProposalParameter extends Parameter {
    private long propIndex;

    public CancelProposalParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeLIntCompact(propIndex);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        propIndex = scr.readCompact().longValue();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("PropIndex", propIndex);
    }
}