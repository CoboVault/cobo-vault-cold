package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class ReapPreimage extends Pallet<ReapPreimageParameter> {

    public ReapPreimage(Network network, int code) {
        super("democracy.readPreimage", network, code);
    }

    @Override
    public ReapPreimageParameter read(ScaleCodecReader scr) {
        return new ReapPreimageParameter(name, network, code, scr);
    }
}
