package com.cobo.coinlib.coins.polkadot.pallets;

import com.cobo.coinlib.coins.polkadot.pallets.balance.Transfer;
import com.cobo.coinlib.coins.polkadot.pallets.balance.TransferKeepAlive;
import com.cobo.coinlib.coins.polkadot.pallets.session.SetKeys;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Bond;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Nominate;
import com.cobo.coinlib.coins.polkadot.pallets.staking.SetController;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Validate;
import com.cobo.coinlib.coins.polkadot.pallets.utility.Batch;
import com.cobo.coinlib.coins.polkadot.Pallet;

public class PalletFactory {
    public static Pallet getPallet(int code) {
        switch (code){
            case 0x0500:
                return new Transfer();
            case 0x0503:
                return new TransferKeepAlive();
            case 0x0900:
                return new SetKeys();
            case 0x0700:
                return new Bond();
            case 0x0704:
                return new Validate();
            case 0x0705:
                return new Nominate();
            case 0x0708:
                return  new SetController();
            case 0x1a00:
                return new Batch();
            default:
                throw new Error("unknown pallet code");
        }
    }
}
