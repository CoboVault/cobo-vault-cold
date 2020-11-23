package com.cobo.coinlib.coins.polkadot.pallets.session;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import java.util.List;

public class SetKeysParameter extends Parameter {
    private final List<byte[]> publicKeys;
    private final byte[] proof;
    public SetKeysParameter(Network network, String name, List<byte[]> publicKeys, byte[] proof) {
        super(network, name);
        this.proof = proof;
        this.publicKeys = publicKeys;
    }
}
