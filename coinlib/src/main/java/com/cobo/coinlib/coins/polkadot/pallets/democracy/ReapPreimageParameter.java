package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ReapPreimageParameter extends Parameter {
    private byte[] proposalHash;
    private long proposalLenUpperBound;
    public ReapPreimageParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(proposalHash);
        scw.writeLIntCompact(proposalLenUpperBound);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        proposalHash = scr.readByteArray(32);
        proposalLenUpperBound = scr.readCompact().longValue();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("ProposalHash", Hex.toHexString(proposalHash))
                .put("ProposalLenUpperBound", proposalLenUpperBound);
    }
}