package com.cobo.coinlib.coins.polkadot.pallets.session;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

import java.util.ArrayList;
import java.util.List;

public class SetKeys extends Pallet<SetKeysParameter> {
    public SetKeys(Network network, int code){
        super("session.setKeys", network, code);
    }

    @Override
    public SetKeysParameter read(ScaleCodecReader scr) {
        return new SetKeysParameter(name, network, code, scr);
    }
}
