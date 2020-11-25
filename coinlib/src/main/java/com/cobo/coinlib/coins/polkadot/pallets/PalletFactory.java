package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.DOT.Dot;
import com.cobo.coinlib.coins.polkadot.KSM.Ksm;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;

public class PalletFactory {
    public static Pallet getPallet(int code, Network network) {
        try {
            if (network.name.equals("Polkadot")) {
                return Dot.pallets.get(code);
            } else {
                return Ksm.pallets.get(code);
            }
        }
        catch (Exception e) {
            throw new Error("unknown pallet code");
        }
    }

    public static Pallet readPallet(ScaleCodecReader scr, Network network) {
        int code = scr.readUint16BE();
        return PalletFactory.getPallet(code, network);
    }
}
