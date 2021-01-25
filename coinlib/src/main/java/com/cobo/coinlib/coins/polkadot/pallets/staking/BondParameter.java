package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class BondParameter extends Parameter {
    private final byte[] publicKey;
    private final BigInteger amount;
    private final Payee payee;

    public BondParameter(String name, Network network, int code, byte[] publicKey, BigInteger amount, Payee payee) {
        super(name, network, code);
        this.publicKey = publicKey;
        this.amount = amount;
        this.payee  = payee;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Controller", AddressCodec.encodeAddress(publicKey, network.SS58Prefix));
        object.put("Value", Utils.getReadableBalanceString(this.network, this.amount));
        payee.writeToJSON(network, object);
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeByteArray(publicKey);
        scw.writeBIntCompact(amount);
        payee.writeTo(scw);
    }
}
