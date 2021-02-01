package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class EnactProposal extends Pallet<EnactProposalParameter> {
    public EnactProposal(Network network, int code) {
        super("democracy.enactProposal", network, code);
    }

    @Override
    public EnactProposalParameter read(ScaleCodecReader scr) {
        return new EnactProposalParameter(name, network, code, scr);
    }
}
