package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.PalletFactory;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.util.Arrays;
import java.util.List;

public class Batch extends Pallet {

    public Batch(Network network) {
        super("utility.batch", network);
    }

    @Override
    public BatchParameter read(ScaleCodecReader scr) {
        List<Pallet> pallets = Arrays.asList();
        int length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            int code = scr.readUint16();
            Pallet pallet = PalletFactory.getPallet(code, network);
            pallet.read(scr);
            pallets.add(pallet);
        }

        return new BatchParameter(network, name, length, pallets);
    }
}
