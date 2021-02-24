package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public interface ParameterCodec {
    void read(ScaleCodecReader scr);

    void write(ScaleCodecWriter scw) throws IOException;

    JSONObject addCallParameter() throws JSONException;
}
