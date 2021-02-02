package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class ForceTransfer extends Pallet<ForceTransferParameter> {
    public ForceTransfer(Network network, int code) {
        super("balance.forceTransfer", network, code);
    }

    @Override
    public ForceTransferParameter read(ScaleCodecReader scr) {
        return new ForceTransferParameter(name, network, code, scr);
    }
}
