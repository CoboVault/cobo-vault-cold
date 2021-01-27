package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VoteParameter extends Parameter {
    private int length;
    private List<byte[]> publicKeys;
    private BigInteger value;

    public VoteParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        length = scr.readCompactInt();
        publicKeys = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            publicKeys.add(scr.readByteArray(32));
        }
        value = scr.readCompact();
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject()
                .put("Length", length)
                .put("Votes", publicKeys.stream().map(p -> AddressCodec.encodeAddress(p, network.SS58Prefix)).collect(Collectors.toList()))
                .put("Value", Utils.getReadableBalanceString(network, value));
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
