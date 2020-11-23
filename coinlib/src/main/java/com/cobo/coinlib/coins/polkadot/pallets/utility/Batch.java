package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.pallets.PalletFactory;
import com.cobo.coinlib.coins.polkadot.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.util.List;

public class Batch extends Pallet {
    private int length;
    private List<Pallet> pallets;

    public Batch() {
        super("utility.batch");
    }

    @Override
    public void read(ScaleCodecReader scr) {
        length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            int code = scr.readUint16();
            Pallet pallet = PalletFactory.getPallet(code);
            pallet.read(scr);
            pallets.add(pallet);
        }
    }
}
