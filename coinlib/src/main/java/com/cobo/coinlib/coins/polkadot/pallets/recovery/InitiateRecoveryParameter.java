package com.cobo.coinlib.coins.polkadot.pallets.recovery;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class InitiateRecoveryParameter extends Parameter {
    private byte[] account;

    public InitiateRecoveryParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        account = readAccount(scr);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {

        return new JSONObject()
                .put("Account", AddressCodec.encodeAddress(account, network.SS58Prefix));
    }


    @Override
    public void write(ScaleCodecWriter scw) throws IOException {
        writeAccount(scw, account);
    }
}
