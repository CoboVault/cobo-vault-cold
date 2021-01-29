package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class Delegate extends Pallet<DelegateParameter> {

    public Delegate(Network network, int code) {
        super("democracy.delegate", network, code);
    }

    @Override
    public DelegateParameter read(ScaleCodecReader scr) {
        return new DelegateParameter(name, network, code, scr);
    }
}
