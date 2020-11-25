package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.UOS.Network;

public class TransferKeepAlive extends TransferBase {
    public TransferKeepAlive(Network network){
        super("balance.transferKeepAlive", network);
    }
}
