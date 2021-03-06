package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class Unlock extends Pallet<UnlockParameter> {

    public Unlock(Network network, int code) {
        super("democracy.unlock", network, code);
    }

    @Override
    public UnlockParameter read(ScaleCodecReader scr) {
        return new UnlockParameter(name, network, code, scr);
    }
}
