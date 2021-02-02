package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.EmptyParameter;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

public class UnDelegate extends Pallet {
    public UnDelegate(Network network, int code) {
        super("democracy.unDelegate", network, code);
    }

    @Override
    public Parameter read(ScaleCodecReader scr) {
        return new EmptyParameter(name, network, code, scr);
    }
}
