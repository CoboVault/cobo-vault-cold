package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class FastTrack extends Pallet<FastTrackParameter> {
    public FastTrack(Network network, int code) {
        super("democracy.fastTrack", network, code);
    }

    @Override
    public FastTrackParameter read(ScaleCodecReader scr) {
        return new FastTrackParameter(name, network, code, scr);
    }
}
