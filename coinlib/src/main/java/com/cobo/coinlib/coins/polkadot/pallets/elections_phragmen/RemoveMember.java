package com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class RemoveMember extends Pallet<RemoveMemberParameter> {
    public RemoveMember(Network network, int code) {
        super("electionsPhragmen.removeMember", network, code);
    }

    @Override
    public RemoveMemberParameter read(ScaleCodecReader scr) {
        return new RemoveMemberParameter(name, network, code, scr);
    }
}
