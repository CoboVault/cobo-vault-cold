package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

public class ValidateParameter extends Parameter {
    private int value; // base 1 * 10^9

    public ValidateParameter(Network network, String name, int value) {
        super(network, name);
        this.value = value;
    }
}
