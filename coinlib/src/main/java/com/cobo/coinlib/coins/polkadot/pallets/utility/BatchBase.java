package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.PalletFactory;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import java.util.ArrayList;
import java.util.List;

public class BatchBase extends Pallet<BatchParameter> {

    public BatchBase(String name, Network network, int code) {
        super(name, network, code);
    }

    @Override
    public BatchParameter read(ScaleCodecReader scr) {
        return new BatchParameter(network, name, code, scr);
    }
}
