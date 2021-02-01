package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class CancelReferendum extends Pallet<CancelReferendumParameter> {

    public CancelReferendum(Network network, int code) {
        super("democracy.cancelReferendum", network, code);
    }

    @Override
    public CancelReferendumParameter read(ScaleCodecReader scr) {
        return new CancelReferendumParameter(name, network, code, scr);
    }
}
