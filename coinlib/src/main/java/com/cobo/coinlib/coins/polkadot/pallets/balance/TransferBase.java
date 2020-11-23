package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.math.BigInteger;

public class TransferBase extends Pallet {
    private byte[] destinationPublicKey;
    private BigInteger amount;
    public TransferBase(String name) {
        super(name);
    }
    public void read(ScaleCodecReader scr){
        this.destinationPublicKey = scr.readByteArray(32);
        this.amount = scr.readCompact();
    }
}
