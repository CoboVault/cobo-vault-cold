package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class ExternalPropose extends Pallet<ExternalProposeParameter> {
    public ExternalPropose(Network network, int code) {
        super("democracy.externalPropose", network, code);
    }

    @Override
    public ExternalProposeParameter read(ScaleCodecReader scr) {
        return new ExternalProposeParameter(name, network, code, scr);
    }
}
