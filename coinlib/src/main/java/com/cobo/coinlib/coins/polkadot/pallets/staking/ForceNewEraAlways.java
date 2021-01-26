package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

public class ForceNewEraAlways extends Pallet {
    public ForceNewEraAlways(Network network, int code) {
        super("staking.forceNewEraAlways", network, code);
    }

    @Override
    public Parameter read(ScaleCodecReader scr) {
        return new Parameter(name, network, code) {
            @Override
            protected JSONObject addCallParameter() throws JSONException {
                return null;
            }
        };
    }
}
