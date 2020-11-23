package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.UOS.Network;

public class Parameter {
    protected Network network;
    public String name;

    public Parameter(Network network, String name) {
        this.network = network;
        this.name = name;
    }
}
