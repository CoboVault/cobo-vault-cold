package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public class VoteParameter extends Parameter {
    private final int length;
    private final List<byte[]> publicKeys;

    public VoteParameter(Network network, String name, int length, List<byte[]> publicKeys) {
        super(network, name);
        this.length = length;
        this.publicKeys = publicKeys;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("length", length);
        object.put("accounts", publicKeys.stream().map(p -> AddressCodec.encodeAddress(p, network.SS58Prefix)).collect(Collectors.toList()));
        return object;
    }
}
