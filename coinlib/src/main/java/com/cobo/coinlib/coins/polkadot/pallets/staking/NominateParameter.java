package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class NominateParameter extends Parameter {
    private final int length;
    private final List<byte[]> publicKeys;

    public NominateParameter(Network network, String name, int length, List<byte[]> publicKeys) {
        super(network, name);
        this.length = length;
        this.publicKeys = publicKeys;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object =  super.toJSON();
        object.put("length", length);
        object.put("nominateAccounts", publicKeys.stream().map(publicKey -> AddressCodec.encodeAddress(publicKey, network.SS58Prefix)).collect(Collectors.toList()));
        return object;
    }
}
