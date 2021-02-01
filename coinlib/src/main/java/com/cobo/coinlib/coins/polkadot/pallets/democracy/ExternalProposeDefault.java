package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class ExternalProposeDefault extends Pallet<ExternalProposeDefaultParameter> {

    public ExternalProposeDefault(Network network, int code) {
        super("democracy.externalProposeDefault", network, code);
    }

    @Override
    public ExternalProposeDefaultParameter read(ScaleCodecReader scr) {
        return new ExternalProposeDefaultParameter(name, network, code, scr);
    }
}
