package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class DelegateParameter extends Parameter {
    private byte[] to;
    private byte conviction;
    private BigInteger balance;
    public DelegateParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        writeAccount(scw, to);
        scw.writeByte(conviction);
        scw.writeBIntCompact(balance);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        to = readAccount(scr);
        conviction = scr.readByte();
        balance = scr.readCompact();
    }

    private String transformConviction(){
        switch (conviction) {
            case 0x00:
                return "None";
            case 0x01:
                return "Locked1x";
            case 0x02:
                return "Locked2x";
            case 0x03:
                return "Locked3x";
            case 0x04:
                return "Locked4x";
            case 0x05:
                return "Locked5x";
            case 0x06:
                return "Locked6x";
            default:
                throw new Error("Unknown Conviction");
        }
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("To", AddressCodec.encodeAddress(to, network.SS58Prefix))
                .put("Conviction", transformConviction())
                .put("Balance", Utils.getReadableBalanceString(network, balance));
    }
}
