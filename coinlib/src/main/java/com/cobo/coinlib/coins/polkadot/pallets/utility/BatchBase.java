package com.cobo.coinlib.coins.polkadot.pallets.utility;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.PalletFactory;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import java.util.Arrays;
import java.util.List;

public class BatchBase extends Pallet {

    public BatchBase(String name, Network network) {
        super(name, network);
    }

    @Override
    public BatchParameter read(ScaleCodecReader scr) {
        List<Parameter> parameters = Arrays.asList();
        int length = scr.readCompactInt();
        for (int i = 0; i < length; i++) {
            int code = scr.readUint16();
            Pallet pallet = PalletFactory.getPallet(code, network);
            Parameter parameter = pallet.read(scr);
            parameters.add(parameter);
        }

        return new BatchParameter(network, name, length, parameters);
    }
}
