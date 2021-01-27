package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.EmptyParameter;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class ForceNewEra extends Pallet<EmptyParameter> {
    public ForceNewEra(Network network, int code) {
        super("staking.forceNewEras", network, code);
    }

    @Override
    public EmptyParameter read(ScaleCodecReader scr) {
        return new EmptyParameter(name, network, code);
    }
}
