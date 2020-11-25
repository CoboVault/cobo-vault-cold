package com.cobo.coinlib.coins.polkadot.pallets.staking;

import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Collectors;

public class BondParameter extends Parameter {
    private final byte[] publicKey;
    private final BigInteger amount;
    private final byte rewardType; // 00: Staked, 01: Stash, 02: Controller, 03: Account(AccountId),
    private final byte[] rewardDestinationPublicKey;

    public BondParameter(Network network, String name, byte[] publicKey, BigInteger amount, byte rewardType, byte[] rewardDestinationPublicKey) {
        super(network, name);
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
        object.put("stashAccount", AddressCodec.encodeAddress(publicKey, network.SS58Prefix));
        object.put("amount", getAmount());
        object.put("rewardType", getRewardType());
        if (rewardDestinationPublicKey.length > 0) {
            object.put("rewardDestinationAccount", AddressCodec.encodeAddress(rewardDestinationPublicKey, network.SS58Prefix));
        }
        return object;
    }
}
