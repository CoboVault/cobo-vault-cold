package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

import java.util.ArrayList;
import java.util.List;

public class Vote extends Pallet<VoteParameter> {
    public Vote(Network network, int code) {
        super("electionsPhragmen.vote", network, code);
    }

    @Override
    public VoteParameter read(ScaleCodecReader scr) {
        return new VoteParameter(name, network, code, scr);
    }
}
