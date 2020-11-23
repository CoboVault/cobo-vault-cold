package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.math.BigInteger;

public class Bond extends Pallet {
    private byte[] publicKey;
    private BigInteger amount;
    private byte rewardType;
    private byte[] rewardDestinationPublicKey;

    public Bond() {
        super("staking.bond");
    }

    @Override
    public void read(ScaleCodecReader scr) {
        publicKey = scr.readByteArray(32);
        amount = scr.readCompact();
        rewardType = scr.readByte();
        switch (rewardType) {
            case 0x00:
                return;
            default:
                this.rewardDestinationPublicKey = scr.readByteArray(32);
        }
    }
}
