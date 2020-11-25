package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

public class Validate extends Pallet {
    public Validate(Network network){
        super("staking.validate", network);
    }

    @Override
    public ValidateParameter read(ScaleCodecReader scr) {

        return new ValidateParameter(network, name, scr.readCompactInt());
    }
}
