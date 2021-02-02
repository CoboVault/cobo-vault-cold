package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class SetBalanceParameter extends Parameter {
    private byte[] who;
    private BigInteger newFree;
    private BigInteger newReserved;

    public SetBalanceParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(who);
        scw.writeBIntCompact(newFree);
        scw.writeBIntCompact(newReserved);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        who = scr.readByteArray(32);
        newFree = scr.readCompact();
        newReserved = scr.readCompact();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Who", AddressCodec.encodeAddress(who, network.SS58Prefix))
                .put("NewFree", Utils.getReadableBalanceString(network, newFree))
                .put("NewReserved", Utils.getReadableBalanceString(network, newReserved));
    }
}
