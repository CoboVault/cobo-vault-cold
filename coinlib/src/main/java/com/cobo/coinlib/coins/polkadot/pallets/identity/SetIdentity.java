package com.cobo.coinlib.coins.polkadot.pallets.identity;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class SetIdentity extends Pallet<SetIdentityParameter> {
    public SetIdentity(Network network, int code) {
        super("identity.setIdentity", network, code);
    }

    @Override
    public SetIdentityParameter read(ScaleCodecReader scr) {
        return new SetIdentityParameter(name, network, code, scr);
    }
}
