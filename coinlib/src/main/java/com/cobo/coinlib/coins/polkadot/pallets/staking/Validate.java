package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

public class Validate extends Pallet {
    private int value; // base 1 * 10^9
    public Validate(){
        super("staking.validate");
    }

    @Override
    public void read(ScaleCodecReader scr) {
        this.value = scr.readCompactInt();
    }
}
