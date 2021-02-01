package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class CancelQueued extends Pallet<CancelQueuedParameter> {

    public CancelQueued(Network network, int code) {
        super("democracy.cancelQueued", network, code);
    }

    @Override
    public CancelQueuedParameter read(ScaleCodecReader scr) {
        return new CancelQueuedParameter(name, network, code, scr);
    }
}
