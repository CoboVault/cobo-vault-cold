package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class Nominate extends Pallet<NominateParameter> {
    public Nominate(Network network, int code) {
        super("staking.nominate", network, code);
    }

    @Override
    public NominateParameter read(ScaleCodecReader scr) {
        return new NominateParameter(name, network, code, scr);
    }
}