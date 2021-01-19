package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransferBase extends Pallet<TransferParameter> {
    public TransferBase(String name, Network network) {
        super(name, network);
    }

    @Override
    public TransferParameter read(ScaleCodecReader scr) {
        return new TransferParameter(network, name, scr.readByteArray(32), scr.readCompact());
    }
}
