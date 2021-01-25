package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vote extends Pallet<VoteParameter> {
    public Vote(Network network, int code) {
        super("electionsPhragmen.vote", network, code);
    }

    @Override
    public VoteParameter read(ScaleCodecReader scr) {
        int length = scr.readCompactInt();
        List<byte[]> publicKeys = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            publicKeys.add(scr.readByteArray(32));
        }
        return new VoteParameter(name, network, code, scr.readCompactInt(), publicKeys, scr.readCompact());
    }
}