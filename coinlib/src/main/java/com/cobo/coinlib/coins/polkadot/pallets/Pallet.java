package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;

public class Pallet {
    public String name;
    protected Network network;

    public Pallet(String name, Network network) {
        this.name = name;
        this.network = network;
    }

    public Parameter read(ScaleCodecReader scr) {
        throw new Error("not implemented");
    }
}
