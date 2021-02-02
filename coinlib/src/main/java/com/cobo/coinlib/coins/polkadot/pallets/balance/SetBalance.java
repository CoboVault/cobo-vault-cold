package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class SetBalance extends Pallet<SetBalanceParameter> {
    public SetBalance(Network network, int code) {
        super("balance.setBalance", network, code);
    }

    @Override
    public SetBalanceParameter read(ScaleCodecReader scr) {
        return new SetBalanceParameter(name, network, code, scr);
    }
}
