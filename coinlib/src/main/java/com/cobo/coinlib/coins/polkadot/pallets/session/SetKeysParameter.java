package com.cobo.coinlib.coins.polkadot.pallets.session;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class SetKeysParameter extends Parameter {
    private final List<byte[]> publicKeys;
    private final byte[] proof;
    public SetKeysParameter(Network network, String name, List<byte[]> publicKeys, byte[] proof) {
        super(network, name);
        this.proof = proof;
        this.publicKeys = publicKeys;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("rotateKeys", publicKeys.stream().map(p -> Hex.toHexString(p)).collect(Collectors.toList()));
        object.put("proof", Hex.toHexString(proof));
        return object;
    }
}
