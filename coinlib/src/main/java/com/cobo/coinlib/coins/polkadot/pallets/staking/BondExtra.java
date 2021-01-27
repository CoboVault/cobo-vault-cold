package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class BondExtra extends Pallet<BondExtraParameter> {
    public BondExtra(Network network, int code) {
        super("staking.bondExtra", network, code);
    }

    @Override
    public BondExtraParameter read(ScaleCodecReader scr) {
        return new BondExtraParameter(network, name, code, scr);
    }
}
