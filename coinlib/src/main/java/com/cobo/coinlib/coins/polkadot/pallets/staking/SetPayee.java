package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class SetPayee extends Pallet<SetPayeeParameter> {
    public SetPayee(Network network, int code) {
        super("staking.setPayee", network, code);
    }

    @Override
    public SetPayeeParameter read(ScaleCodecReader scr) {
        return new SetPayeeParameter(name, network, code, scr);
    }
}
