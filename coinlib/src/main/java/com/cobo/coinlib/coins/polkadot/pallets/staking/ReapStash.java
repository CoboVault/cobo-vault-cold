package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class ReapStash extends Pallet<ReapStashParameter> {
    public ReapStash(Network network, int code) {
        super("staking.reapStash", network, code);
    }

    @Override
    public ReapStashParameter read(ScaleCodecReader scr) {
        return new ReapStashParameter(name, network, code, scr);
    }
}
