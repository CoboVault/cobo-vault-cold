package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

import java.util.ArrayList;
import java.util.List;

public class CancelDeferredSlash extends Pallet<CancelDeferredSlashParameter> {
    public CancelDeferredSlash(Network network, int code) {
        super("staking.cancelDeferredSlash", network, code);
    }

    @Override
    public CancelDeferredSlashParameter read(ScaleCodecReader scr) {
        long eraIndex = scr.readUint32();
        int length = scr.readCompactInt();
        List<Long> slashIndices = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            slashIndices.add(scr.readUint32());
        }
        return new CancelDeferredSlashParameter(name, network, code, eraIndex, length, slashIndices);
    }
}
