package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.util.List;

public class Nominate extends Pallet {
    private int length;
    private List<byte[]> publicKeys;
    public Nominate(){
        super("staking.nominate");
    }

    @Override
    public void read(ScaleCodecReader scr) {
        length = scr.readUByte();
        for (int i = 0; i< length; i++) {
            publicKeys.add(scr.readByteArray(32));
        }
    }
}