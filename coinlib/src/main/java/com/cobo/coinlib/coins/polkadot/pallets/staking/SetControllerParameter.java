package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

public class SetControllerParameter extends Parameter {
    private final byte[] publicKey;

    public SetControllerParameter(Network network, String name, byte[] publicKey) {
        super(network, name);
        this.publicKey = publicKey;
    }
}
