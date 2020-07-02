package com.cobo.coinlib.coins.XTN;

import com.cobo.coinlib.coins.BTC.Btc;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.Coin;
import com.cobo.coinlib.utils.Coins;

import org.json.JSONException;
import org.json.JSONObject;

public class Xtn extends Btc {
    public Xtn(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.XTN.coinCode();
    }

    public static class Tx extends Btc.Tx {

        public Tx(JSONObject signTxObject, String coinCode) throws JSONException,
                InvalidTransactionException {
            super(signTxObject, coinCode);
        }
    }
}
