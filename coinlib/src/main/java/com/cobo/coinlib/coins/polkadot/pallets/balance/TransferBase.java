package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransferBase extends Pallet<TransferParameter> {
    public TransferBase(String name, Network network, int code) {
        super(name, network, code);
    }

    @Override
    public TransferParameter read(ScaleCodecReader scr) {
        return new TransferParameter(name, network, this.code, scr.readByteArray(32), scr.readCompact());
    }
}
