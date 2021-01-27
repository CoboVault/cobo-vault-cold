package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class CancelDeferredSlashParameter extends Parameter {
    private final long eraIndex;
    private final int length;
    private final List<Long> slashIndices;

    public CancelDeferredSlashParameter(String name, Network network, int code, long eraIndex, int length, List<Long> slashIndices) {
        super(name, network, code);
        this.eraIndex = eraIndex;
        this.length = length;
        this.slashIndices = slashIndices;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("EraIndex", eraIndex)
                .put("Length", length)
                .put("SlashIndices", toJSONArray());
    }

    private JSONArray toJSONArray() {
        JSONArray array = new JSONArray();
        for (Long slashIndex : slashIndices
        ) {
            array.put(slashIndex);
        }
        return array;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeUint32(eraIndex);
        scw.writeCompact(length);
        for (Long slashIndex : slashIndices
        ) {
            scw.writeUint32(slashIndex);
        }
    }
}
