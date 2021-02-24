package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Propose extends Pallet<ProposeParameter> {
    public Propose(Network network, int code) {
        super("democracy.propose", network, code);
    }

    @Override
    public ProposeParameter read(ScaleCodecReader scr) {
        return new ProposeParameter(name, network, code, scr);
    }
}
