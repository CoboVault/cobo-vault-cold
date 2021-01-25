package com.cobo.coinlib.coins.polkadot.pallets.session;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetKeys extends Pallet<SetKeysParameter> {
    public SetKeys(Network network, int code){
        super("session.setKeys", network, code);
    }

    @Override
    public SetKeysParameter read(ScaleCodecReader scr) {
        List<byte[]> publicKeys = new ArrayList<>();
        byte[] proof;
        for (int i = 0 ; i < 5 ; i++ ){
            publicKeys.add(scr.readByteArray(32));
        }
        proof = scr.readByteArray(1);
        return new SetKeysParameter(network, name, code, publicKeys, proof);
    }
}
