package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

public class SetController extends Pallet<SetControllerParameter> {
    private byte[] publicKey;
    public SetController(Network network){
        super("staking.setController", network);
    }

    @Override
    public SetControllerParameter read(ScaleCodecReader scr) {
        return new SetControllerParameter(network, name, scr.readByteArray(32));
    }
}
