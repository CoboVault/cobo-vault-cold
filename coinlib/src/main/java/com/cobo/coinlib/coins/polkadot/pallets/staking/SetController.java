package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

public class SetController extends Pallet {
    private byte[] publicKey;
    public SetController(){
        super("staking.setController");
    }

    @Override
    public void read(ScaleCodecReader scr) {
        publicKey = scr.readByteArray(32);
    }
}
