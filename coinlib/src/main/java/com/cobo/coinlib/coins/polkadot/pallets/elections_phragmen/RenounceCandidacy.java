package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class RenounceCandidacy extends Pallet<RenounceCandidacyParameter> {

    public RenounceCandidacy(Network network, int code) {
        super("electionsPhragmen.renounceCandidacy", network, code);
    }

    @Override
    public RenounceCandidacyParameter read(ScaleCodecReader scr) {
        return new RenounceCandidacyParameter(name, network, code, scr);
    }
}
