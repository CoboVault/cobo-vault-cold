package com.cobo.coinlib.coins.polkadot.pallets.recovery;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class InitiateRecoveryParameter extends Parameter {
    private byte[] account;

    public InitiateRecoveryParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        account = scr.readByteArray(32);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {

        return new JSONObject()
                .put("Account", AddressCodec.encodeAddress(account, network.SS58Prefix));
    }


    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(account);
    }
}
