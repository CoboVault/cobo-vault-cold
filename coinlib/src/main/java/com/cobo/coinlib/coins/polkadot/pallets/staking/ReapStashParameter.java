package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ReapStashParameter extends Parameter {
    private byte[] stashAccountPublicKey;
    private long numSlashingSpans;

    public ReapStashParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        stashAccountPublicKey = scr.readByteArray(32);
        numSlashingSpans = scr.readUint32();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Stash", AddressCodec.encodeAddress(stashAccountPublicKey, network.SS58Prefix))
                .put("NumSlashingSpans", numSlashingSpans);
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeByteArray(stashAccountPublicKey);
        scw.writeUint32(numSlashingSpans);
    }
}
