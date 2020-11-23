package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import java.util.List;

public class BatchParameter extends Parameter {
    private final int length;
    private final List<Pallet> pallets;

    public BatchParameter(Network network, String name, int length, List<Pallet> pallets) {
        super(network, name);
        this.length = length;
        this.pallets = pallets;
    }
}
