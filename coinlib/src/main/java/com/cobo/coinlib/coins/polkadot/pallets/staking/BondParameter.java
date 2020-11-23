package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import java.math.BigInteger;

public class BondParameter extends Parameter {
    private final byte[] publicKey;
    private final BigInteger amount;
    private final byte rewardType;
    private final byte[] rewardDestinationPublicKey;

    public BondParameter(Network network, String name, byte[] publicKey, BigInteger amount, byte rewardType, byte[] rewardDestinationPublicKey) {
        super(network, name);
        this.publicKey = publicKey;
        this.amount = amount;
        this.rewardType = rewardType;
        this.rewardDestinationPublicKey = rewardDestinationPublicKey;
    }
}
