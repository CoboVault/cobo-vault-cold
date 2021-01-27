package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class SetHistoryDepth extends Pallet<SetHistoryDepthParameter> {

    public SetHistoryDepth(Network network, int code) {
        super("staking.setHistoryDepth", network, code);
    }

    @Override
    public SetHistoryDepthParameter read(ScaleCodecReader scr) {
        return new SetHistoryDepthParameter(name, network, code, scr);
    }
}
