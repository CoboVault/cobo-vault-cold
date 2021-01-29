package com.cobo.coinlib.coins.polkadot.pallets.proxy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class AddProxy extends Pallet<AddProxyParameter> {
    public AddProxy(Network network, int code) {
        super("proxy.addProxy", network, code);
    }

    @Override
    public AddProxyParameter read(ScaleCodecReader scr) {
        return new AddProxyParameter(name, network, code, scr);
    }
}
