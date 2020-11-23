package com.cobo.coinlib.coins.polkadot.pallets.session;

import com.cobo.coinlib.coins.polkadot.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.util.List;

public class SetKeys extends Pallet {
    public List<byte[]> publicKeys;
    public byte[] proof;
    public SetKeys(){
        super("session.setKeys");
    }

    @Override
    public void read(ScaleCodecReader scr) {
        for (int i = 0 ; i < 5 ; i++ ){
            publicKeys.add(scr.readByteArray(32));
        }
        proof = scr.readByteArray(1);
    }
}
