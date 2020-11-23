package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import java.util.List;

public class NominateParameter extends Parameter {
    private final int length;
    private final List<byte[]> publicKeys;

    public NominateParameter(Network network, String name, int length, List<byte[]> publicKeys) {
        super(network, name);
        this.length = length;
        this.publicKeys = publicKeys;
    }
}
