package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Bond extends Pallet<BondParameter> {
    public Bond(Network network, int code) {
        super("staking.bond", network, code);
    }

    @Override
    public BondParameter read(ScaleCodecReader scr) {
        return new BondParameter(name, network, code, scr);
    }
}
