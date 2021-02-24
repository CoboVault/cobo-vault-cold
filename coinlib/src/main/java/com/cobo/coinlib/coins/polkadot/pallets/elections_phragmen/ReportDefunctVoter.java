package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class ReportDefunctVoter extends Pallet<ReportDefunctVoterParameter> {
    public ReportDefunctVoter(Network network, int code) {
        super("electionsPhragmen.reportDefunctVoter", network, code);
    }

    @Override
    public ReportDefunctVoterParameter read(ScaleCodecReader scr) {
        return new ReportDefunctVoterParameter(name, network, code, scr);
    }
}
