package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class IncreaseValidatorCount extends Pallet<IncreaseValidatorCountParameter> {
    public IncreaseValidatorCount(Network network, int code) {
        super("staking.increaseValidatorCount", network, code);
    }

    @Override
    public IncreaseValidatorCountParameter read(ScaleCodecReader scr) {
        return new IncreaseValidatorCountParameter(name, network, code, scr.readCompactInt());
    }
}
