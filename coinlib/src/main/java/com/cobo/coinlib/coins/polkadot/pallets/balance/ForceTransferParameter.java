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

public class ForceTransferParameter extends Parameter {
    private byte[] source;
    private byte[] dest;
    private BigInteger value;

    public ForceTransferParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeByteArray(source);
        scw.writeByteArray(dest);
        scw.writeBIntCompact(value);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        source = scr.readByteArray(32);
        dest = scr.readByteArray(32);
        value = scr.readCompact();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("Source", AddressCodec.encodeAddress(source, network.SS58Prefix))
                .put("Dest", AddressCodec.encodeAddress(dest, network.SS58Prefix))
                .put("Value", Utils.getReadableBalanceString(network, value));
    }
}
