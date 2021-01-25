package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.EmptyParameter;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class Chill extends Pallet<EmptyParameter> {

    public Chill(String name, Network network, int code) {
        super("staking.chill", network, code);
    }

    @Override
    public EmptyParameter read(ScaleCodecReader scr) {
        return new EmptyParameter(name, network, code);
    }
}
