package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class Second extends Pallet<SecondParameter> {
    public Second(Network network, int code) {
        super("democracy.second", network, code);
    }

    @Override
    public SecondParameter read(ScaleCodecReader scr) {
        return new SecondParameter(name, network, code, scr);
    }
}
