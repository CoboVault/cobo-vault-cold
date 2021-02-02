package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class EnactProposalParameter extends Parameter {
    private byte[] proposalHash;
    private long index;

    public EnactProposalParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(proposalHash);
        scw.writeUint32(index);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        proposalHash = scr.readByteArray(32);
        index = scr.readUint32();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("ProposalHash", Hex.toHexString(proposalHash))
                .put("Index", index);
    }
}
