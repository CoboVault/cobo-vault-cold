package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;

import org.json.JSONObject;

public abstract class Pallet <T extends Parameter> {
    public String name;
    protected Network network;

    public Pallet(String name, Network network) {
        this.name = name;
        this.network = network;
    }

    public abstract T read(ScaleCodecReader scr);
}
