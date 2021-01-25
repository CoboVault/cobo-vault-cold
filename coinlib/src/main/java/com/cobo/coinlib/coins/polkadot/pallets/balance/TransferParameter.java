package com.cobo.coinlib.coins.polkadot.pallets.balance;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class TransferParameter extends Parameter {
    private final byte[] destinationPublicKey;
    private final BigInteger amount;

    public TransferParameter(Network network, String name, int code, byte[] destinationPublicKey,
                             BigInteger amount) {
        super(network, name, code);
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

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("Destination", getDestination());
        object.put("Value", getAmount());
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeByteArray(destinationPublicKey);
        scw.writeBIntCompact(amount);
    }
}
