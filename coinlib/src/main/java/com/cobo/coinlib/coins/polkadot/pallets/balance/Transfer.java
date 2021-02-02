package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class Transfer extends Pallet<TransferParameter> {
    public Transfer(Network network, int code) {
        super("balance.transfer", network, code);
    }
    @Override
    public TransferParameter read(ScaleCodecReader scr) {
        return new TransferParameter(name, network, this.code, scr);
    }
}
