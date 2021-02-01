package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class ExternalProposeMajority extends Pallet<ExternalProposeMajorityParameter> {

    public ExternalProposeMajority(Network network, int code) {
        super("democracy.externalProposeMajority", network, code);
    }

    @Override
    public ExternalProposeMajorityParameter read(ScaleCodecReader scr) {
        return new ExternalProposeMajorityParameter(name, network, code, scr);
    }
}
