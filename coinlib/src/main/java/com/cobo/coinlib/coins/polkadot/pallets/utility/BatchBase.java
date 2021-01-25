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
        List<Parameter> parameters = new ArrayList<>();
        int length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            int code = scr.readUint16BE();
            Pallet pallet = PalletFactory.getPallet(code, network);
            Parameter parameter = pallet.read(scr);
            parameters.add(parameter);
        }

        return new BatchParameter(network, name, code, length, parameters);
    }
}