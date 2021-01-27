package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

import java.util.ArrayList;
import java.util.List;

public class SetInvulnerables extends Pallet<SetInvulnerablesParameter> {
    public SetInvulnerables(Network network, int code){
        super("staking.setInvulnerables", network, code);
    }

    @Override
    public SetInvulnerablesParameter read(ScaleCodecReader scr) {
        return new SetInvulnerablesParameter(name, network, code, scr);
    }
}
