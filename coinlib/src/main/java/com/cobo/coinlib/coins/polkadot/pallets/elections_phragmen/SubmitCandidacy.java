package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class SubmitCandidacy extends Pallet<SubmitCandidacyParameter> {
    public SubmitCandidacy(Network network, int code) {
        super("electionsPhragmen.submitCandidacy", network, code);
    }

    @Override
    public SubmitCandidacyParameter read(ScaleCodecReader scr) {
        return new SubmitCandidacyParameter(name, network, code, scr);
    }
}
