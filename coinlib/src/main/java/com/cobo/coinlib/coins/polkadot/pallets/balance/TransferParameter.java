package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransferParameter extends Parameter {
    private final byte[] destinationPublicKey;
    private final BigInteger amount;

    public TransferParameter(Network network, String name, byte[] destinationPublicKey, BigInteger amount) {
        super(network, name);
        this.destinationPublicKey = destinationPublicKey;
        this.amount = amount;
    }

    public String getDestination() {
        return AddressCodec.encodeAddress(destinationPublicKey, this.network.SS58Prefix);
    }

    public String getAmount() {
        return new BigDecimal(amount)
                .divide(BigDecimal.TEN.pow(network.decimals), Math.min(network.decimals, 8), BigDecimal.ROUND_HALF_UP)
                .stripTrailingZeros().toPlainString();
    }
}
