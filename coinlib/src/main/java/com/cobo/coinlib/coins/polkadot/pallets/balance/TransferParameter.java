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

public class TransferParameter extends Parameter {
    private byte[] destinationPublicKey;
    private BigInteger amount;

    public TransferParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    public String getDestination() {
        return AddressCodec.encodeAddress(destinationPublicKey, this.network.SS58Prefix);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        destinationPublicKey = scr.readByteArray(32);
        amount = scr.readCompact();
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
