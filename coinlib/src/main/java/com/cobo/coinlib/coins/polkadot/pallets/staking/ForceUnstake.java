package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class ForceUnstake extends Pallet<ForceUnstakeParameter> {

    public ForceUnstake(Network network, int code) {
        super("staking.forceUnstake", network, code);
    }

    @Override
    public ForceUnstakeParameter read(ScaleCodecReader scr) {
        return new ForceUnstakeParameter(name, network, code, scr.readByteArray(32), scr.readUint32());
    }
}
