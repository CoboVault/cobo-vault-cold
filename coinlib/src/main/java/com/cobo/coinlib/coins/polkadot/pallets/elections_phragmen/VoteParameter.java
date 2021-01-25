package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class VoteParameter extends Parameter {
    private final int length;
    private final List<byte[]> publicKeys;
    private final BigInteger value;

    public VoteParameter(String name, Network network, int code, int length, List<byte[]> publicKeys, BigInteger value) {
        super(name, network, code);
        this.length = length;
        this.publicKeys = publicKeys;
        this.value = value;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("length", length)
                .put("votes", publicKeys.stream().map(p -> AddressCodec.encodeAddress(p, network.SS58Prefix)).collect(Collectors.toList()))
                .put("value", Utils.getReadableBalanceString(network, value));
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeCompact(length);
        for (int i = 0; i < publicKeys.size(); i++) {
            scw.writeByteArray(publicKeys.get(i));
        }
        scw.writeBIntCompact(value);
    }
}
