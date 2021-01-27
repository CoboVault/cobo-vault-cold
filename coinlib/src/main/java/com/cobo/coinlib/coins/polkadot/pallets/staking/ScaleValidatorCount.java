package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class ScaleValidatorCount extends Pallet<ScaleValidatorCountParameter> {

    public ScaleValidatorCount(Network network, int code) {
        super("staking.scaleValidatorCount", network, code);
    }

    @Override
    public ScaleValidatorCountParameter read(ScaleCodecReader scr) {
        return new ScaleValidatorCountParameter(name, network, code, scr);
    }
}
