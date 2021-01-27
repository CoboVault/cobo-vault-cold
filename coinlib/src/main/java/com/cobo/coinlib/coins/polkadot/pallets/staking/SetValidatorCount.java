package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class SetValidatorCount extends Pallet<SetValidatorCountParameter> {

    public SetValidatorCount(Network network, int code) {
        super("staking.setValidatorCount", network, code);
    }

    @Override
    public SetValidatorCountParameter read(ScaleCodecReader scr) {
        return new SetValidatorCountParameter(name, network, code, scr);
    }
}
