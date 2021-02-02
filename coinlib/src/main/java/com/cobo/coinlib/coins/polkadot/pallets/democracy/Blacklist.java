package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class Blacklist extends Pallet<BlacklistParameter> {
    public Blacklist(Network network, int code) {
        super("democracy.blacklist", network, code);
    }

    @Override
    public BlacklistParameter read(ScaleCodecReader scr) {
        return new BlacklistParameter(name, network, code, scr);
    }
}
