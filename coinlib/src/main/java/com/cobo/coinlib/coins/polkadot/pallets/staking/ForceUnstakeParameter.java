package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ForceUnstakeParameter extends Parameter {
    private final byte[] stashAccountPublicKey;
    private final long numSlashingSpans;
    public ForceUnstakeParameter(String name, Network network, int code, byte[] stashAccountPublicKey, long numSlashingSpans) {
        super(name, network, code);
        this.stashAccountPublicKey = stashAccountPublicKey;
        this.numSlashingSpans = numSlashingSpans;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("stash", AddressCodec.encodeAddress(stashAccountPublicKey, network.SS58Prefix))
                .put("numSlashingSpans", numSlashingSpans);
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeByteArray(stashAccountPublicKey);
        scw.writeUint32(numSlashingSpans);
    }
}
