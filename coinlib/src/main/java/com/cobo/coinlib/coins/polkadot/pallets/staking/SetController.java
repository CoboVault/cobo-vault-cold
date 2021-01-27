package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

public class SetController extends Pallet<SetControllerParameter> {
    public SetController(Network network, int code){
        super("staking.setController", network, code);
    }

    @Override
    public SetControllerParameter read(ScaleCodecReader scr) {
        return new SetControllerParameter(name, network, code, scr);
    }
}
