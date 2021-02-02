package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class RemoveVote extends Pallet<RemoveVoteParameter> {
    public RemoveVote(Network network, int code) {
        super("democracy.removeVote", network, code);
    }

    @Override
    public RemoveVoteParameter read(ScaleCodecReader scr) {
        return new RemoveVoteParameter(name, network, code, scr);
    }
}
