package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.math.BigInteger;

public class Bond extends Pallet<BondParameter> {
    private byte[] publicKey;
    private BigInteger amount;
    private byte rewardType;
    private byte[] rewardDestinationPublicKey;

    public Bond(Network network, int code) {
        super("staking.bond", network, code);
    }

    @Override
    public BondParameter read(ScaleCodecReader scr) {
        byte[] publicKey;
        BigInteger amount;
        byte rewardType;
        byte[] rewardDestinationPublicKey = {};

        publicKey = scr.readByteArray(32);
        amount = scr.readCompact();
        rewardType = scr.readByte();
        switch (rewardType) {
            case 0x00:
            case 0x01:
            case 0x02:
                break;
            default:
                rewardDestinationPublicKey = scr.readByteArray(32);
        }
        return new BondParameter(network, name, code, publicKey, amount, rewardType, rewardDestinationPublicKey);
    }
}
