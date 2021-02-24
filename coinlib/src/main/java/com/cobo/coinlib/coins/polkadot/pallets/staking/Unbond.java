package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Unbond extends Pallet<UnbondParameter> {
    public Unbond(Network network, int code) {
        super("staking.unbond", network, code);
    }

    @Override
    public UnbondParameter read(ScaleCodecReader scr) {
        return new UnbondParameter(name, network, code, scr);
    }
}
