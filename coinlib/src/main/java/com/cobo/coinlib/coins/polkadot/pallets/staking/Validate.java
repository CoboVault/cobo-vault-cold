package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

public class Validate extends Pallet<ValidateParameter> {
    public Validate(Network network, int code){
        super("staking.validate", network, code);
    }

    @Override
    public ValidateParameter read(ScaleCodecReader scr) {
        return new ValidateParameter(name, network, code, scr.readCompactInt());
    }
}
