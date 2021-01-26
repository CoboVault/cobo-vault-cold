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
        List<byte[]> publicKeys = new ArrayList<>();
        int length = scr.readUByte();
        for (int i = 0; i< length; i++) {
            publicKeys.add(scr.readByteArray(32));
        }
        return new SetInvulnerablesParameter(name, network, code, length, publicKeys);
    }
}
