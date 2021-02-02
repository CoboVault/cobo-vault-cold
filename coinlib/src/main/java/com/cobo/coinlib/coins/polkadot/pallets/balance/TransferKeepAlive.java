package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class TransferKeepAlive extends Pallet<TransferParameter> {
    public TransferKeepAlive(Network network, int code) {
        super("balance.transferKeepAlive", network, code);
    }
    @Override
    public TransferParameter read(ScaleCodecReader scr) {
        return new TransferParameter(name, network, this.code, scr);
    }
}
