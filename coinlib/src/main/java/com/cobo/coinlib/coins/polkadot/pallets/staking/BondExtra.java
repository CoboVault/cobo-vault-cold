package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class BondExtra extends Pallet<BondExtraParameter> {
    public BondExtra(Network network, int code) {
        super("staking.bondExtra", network, code);
    }

    @Override
    public BondExtraParameter read(ScaleCodecReader scr) {
        return new BondExtraParameter(network, name, code, scr);
    }
}
