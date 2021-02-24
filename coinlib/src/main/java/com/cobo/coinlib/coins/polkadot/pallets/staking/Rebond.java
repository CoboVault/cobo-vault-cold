package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Rebond extends Pallet<RebondParameter> {

    public Rebond(Network network, int code) {
        super("staking.rebond", network, code);
    }

    @Override
    public RebondParameter read(ScaleCodecReader scr) {
        return new RebondParameter(name, network, code, scr);
    }
}
