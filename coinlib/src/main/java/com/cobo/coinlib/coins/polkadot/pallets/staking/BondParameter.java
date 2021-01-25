package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class BondParameter extends Parameter {
    private final byte[] publicKey;
    private final BigInteger amount;
    private final byte rewardType; // 00: Staked, 01: Stash, 02: Controller, 03: Account(AccountId),
    private final byte[] rewardDestinationPublicKey;

    public BondParameter(String name, Network network, int code, byte[] publicKey, BigInteger amount, byte rewardType, byte[] rewardDestinationPublicKey) {
        super(name, network, code);
        this.publicKey = publicKey;
        this.amount = amount;
        this.rewardType = rewardType;
        this.rewardDestinationPublicKey = rewardDestinationPublicKey;
    }

    public String getAmount() {
        return new BigDecimal(amount)
                .divide(BigDecimal.TEN.pow(network.decimals), Math.min(network.decimals, 8), BigDecimal.ROUND_HALF_UP)
                .stripTrailingZeros().toPlainString();
    }

    public String getRewardType() {
        switch (rewardType) {
            case 0x00:
                return "Staked";
            case 0x01:
                return "Stash";
            case 0x02:
                return "Controller";
            case 0x03:
                return "Account";
            default:
                throw new Error("invalid reward type");
        }
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject object = super.toJSON();
        object.put("Controller", AddressCodec.encodeAddress(publicKey, network.SS58Prefix));
        object.put("Value", Utils.getReadableBalanceString(this.network, this.amount));
        object.put("Payee", getRewardType());
        if (rewardDestinationPublicKey.length > 0) {
            object.put("RewardDestination", AddressCodec.encodeAddress(rewardDestinationPublicKey, network.SS58Prefix));
        }
        return object;
    }

    @Override
    public void writeTo(ScaleCodecWriter scw) throws IOException {
        super.writeTo(scw);
        scw.writeByteArray(publicKey);
        scw.writeBIntCompact(amount);
        scw.writeByte(rewardType);
        scw.writeByteArray(rewardDestinationPublicKey);
    }
}
