package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RemoveMemberParameter extends Parameter {
    private byte[] who;
    private boolean hasReplacement;

    public RemoveMemberParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(who);
        scw.writeBoolean(hasReplacement);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        who = scr.readByteArray(32);
        hasReplacement = scr.readBoolean();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Who", AddressCodec.encodeAddress(who, network.SS58Prefix))
                .put("HasReplacement", hasReplacement);
    }
}
