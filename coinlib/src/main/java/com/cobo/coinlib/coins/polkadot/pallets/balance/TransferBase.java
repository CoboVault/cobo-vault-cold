package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class TransferBase extends Pallet<TransferParameter> {
    public TransferBase(String name, Network network, int code) {
        super(name, network, code);
    }

    @Override
    public TransferParameter read(ScaleCodecReader scr) {
        return new TransferParameter(name, network, this.code, scr);
    }
}
