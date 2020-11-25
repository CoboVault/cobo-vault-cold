package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.UOS.Network;

public class Batch extends BatchBase {
    public Batch(Network network) {
        super("utility.batch", network);
    }
}
