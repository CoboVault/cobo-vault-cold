package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

public class SetControllerParameter extends Parameter {
    private final byte[] publicKey;

    public SetControllerParameter(Network network, String name, byte[] publicKey) {
        super(network, name);
        this.publicKey = publicKey;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("controller", AddressCodec.encodeAddress(publicKey, network.SS58Prefix));
        return object;
    }
}
