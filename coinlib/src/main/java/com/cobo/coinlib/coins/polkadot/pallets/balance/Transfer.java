package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.UOS.Network;

public class Transfer extends TransferBase {
    public Transfer(Network network, int code){
        super("balance.transfer", network, code);
    }
}
