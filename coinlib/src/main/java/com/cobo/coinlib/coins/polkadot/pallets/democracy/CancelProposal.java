package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class CancelProposal extends Pallet<CancelProposalParameter> {
    public CancelProposal(Network network, int code) {
        super("democracy.cancelProposal", network, code);
    }

    @Override
    public CancelProposalParameter read(ScaleCodecReader scr) {
        return new CancelProposalParameter(name, network, code, scr);
    }
}
