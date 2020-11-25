package com.cobo.coinlib.coins.polkadot.pallets.session;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.util.Arrays;
import java.util.List;

public class SetKeys extends Pallet {
    public SetKeys(Network network){
        super("session.setKeys", network);
    }

    @Override
    public SetKeysParameter read(ScaleCodecReader scr) {
        List<byte[]> publicKeys = Arrays.asList();
        byte[] proof;
        for (int i = 0 ; i < 5 ; i++ ){
            publicKeys.add(scr.readByteArray(32));
        }
        proof = scr.readByteArray(1);
        return new SetKeysParameter(network, name, publicKeys, proof);
    }
}
