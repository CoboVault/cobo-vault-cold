package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class SetPayee extends Pallet<SetPayeeParameter> {
    public SetPayee(String name, Network network, int code) {
        super("staking.set_payee", network, code);
    }

    @Override
    public SetPayeeParameter read(ScaleCodecReader scr) {
        Payee payee = Payee.readToPayee(scr);
        return new SetPayeeParameter(name, network, code, payee);
    }
}
