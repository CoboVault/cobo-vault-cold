package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class RenounceCandidacy extends Pallet<RenounceCandidacyParameter> {

    public RenounceCandidacy(Network network, int code) {
        super("electionsPhragmen.renounceCandidacy", network, code);
    }

    @Override
    public RenounceCandidacyParameter read(ScaleCodecReader scr) {
        return new RenounceCandidacyParameter(name, network, code, scr);
    }
}
