package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.UOS.Network;

public class TransferKeepAlive extends TransferBase {
    public TransferKeepAlive(Network network, int code) {
        super("balance.transferKeepAlive", network, code);
    }
}
