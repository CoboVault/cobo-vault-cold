package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class WithdrawUnbonded extends Pallet<WithdrawUnbondedParameter> {

    public WithdrawUnbonded(String name, Network network, int code) {
        super("staking.withdraw_unbonded", network, code);
    }

    @Override
    public WithdrawUnbondedParameter read(ScaleCodecReader scr) {
        return new WithdrawUnbondedParameter(name, network, code, scr.readUint32());
    }
}
