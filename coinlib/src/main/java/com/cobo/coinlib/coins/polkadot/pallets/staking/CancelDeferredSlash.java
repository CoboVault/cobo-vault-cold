package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class CancelDeferredSlash extends Pallet<CancelDeferredSlashParameter> {
    public CancelDeferredSlash(Network network, int code) {
        super("staking.cancelDeferredSlash", network, code);
    }

    @Override
    public CancelDeferredSlashParameter read(ScaleCodecReader scr) {
        return new CancelDeferredSlashParameter(name, network, code, scr);
    }
}
