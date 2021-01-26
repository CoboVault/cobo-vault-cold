package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class PayoutStakersParameter extends Parameter {
    private byte[] publicKey;
    private long eraIndex;
    public PayoutStakersParameter(String name, Network network, int code, byte[] publicKey, long eraIndex) {
        super(name, network, code);
        this.publicKey = publicKey;
        this.eraIndex = eraIndex;
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("ValidatorSlash", AddressCodec.encodeAddress(publicKey, this.network.SS58Prefix))
                .put("Era", eraIndex);
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeByteArray(publicKey);
        scw.writeUint32(eraIndex);
    }
}
