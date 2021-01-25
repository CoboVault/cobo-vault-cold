package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class TransferParameter extends Parameter {
    private final byte[] destinationPublicKey;
    private final BigInteger amount;

    public TransferParameter(String name, Network network, int code, byte[] destinationPublicKey,
                             BigInteger amount) {
        super(name, network, code);
        this.destinationPublicKey = destinationPublicKey;
        this.amount = amount;
    }

    public String getDestination() {
        return AddressCodec.encodeAddress(destinationPublicKey, this.network.SS58Prefix);
    }

    @Override
    public JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("Dest", getDestination())
                .put("Value", Utils.getReadableBalanceString(this.network, this.amount));
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeByteArray(destinationPublicKey);
        scw.writeBIntCompact(amount);
    }
}
