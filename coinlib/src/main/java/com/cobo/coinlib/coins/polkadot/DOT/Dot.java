/*
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cobo.coinlib.coins.polkadot.DOT;

import com.cobo.coinlib.coins.AbsCoin;
import com.cobo.coinlib.coins.AbsDeriver;
import com.cobo.coinlib.coins.AbsTx;
import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.balance.Transfer;
import com.cobo.coinlib.coins.polkadot.pallets.balance.TransferKeepAlive;
import com.cobo.coinlib.coins.polkadot.pallets.democracy.Delegate;
import com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen.Vote;
import com.cobo.coinlib.coins.polkadot.pallets.identity.SetIdentity;
import com.cobo.coinlib.coins.polkadot.pallets.proxy.AddProxy;
import com.cobo.coinlib.coins.polkadot.pallets.session.SetKeys;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Bond;
import com.cobo.coinlib.coins.polkadot.pallets.staking.BondExtra;
import com.cobo.coinlib.coins.polkadot.pallets.staking.CancelDeferredSlash;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Chill;
import com.cobo.coinlib.coins.polkadot.pallets.staking.ForceNewEra;
import com.cobo.coinlib.coins.polkadot.pallets.staking.ForceNewEraAlways;
import com.cobo.coinlib.coins.polkadot.pallets.staking.ForceNoEras;
import com.cobo.coinlib.coins.polkadot.pallets.staking.ForceUnstake;
import com.cobo.coinlib.coins.polkadot.pallets.staking.IncreaseValidatorCount;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Nominate;
import com.cobo.coinlib.coins.polkadot.pallets.staking.PayoutStakers;
import com.cobo.coinlib.coins.polkadot.pallets.staking.ReapStash;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Rebond;
import com.cobo.coinlib.coins.polkadot.pallets.staking.ScaleValidatorCount;
import com.cobo.coinlib.coins.polkadot.pallets.staking.SetController;
import com.cobo.coinlib.coins.polkadot.pallets.staking.SetHistoryDepth;
import com.cobo.coinlib.coins.polkadot.pallets.staking.SetInvulnerables;
import com.cobo.coinlib.coins.polkadot.pallets.staking.SetPayee;
import com.cobo.coinlib.coins.polkadot.pallets.staking.SetValidatorCount;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Unbond;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Validate;
import com.cobo.coinlib.coins.polkadot.pallets.staking.WithdrawUnbonded;
import com.cobo.coinlib.coins.polkadot.pallets.utility.Batch;
import com.cobo.coinlib.coins.polkadot.pallets.utility.BatchAll;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.Coin;
import com.cobo.coinlib.utils.B58;
import com.cobo.coinlib.utils.Coins;

import org.bouncycastle.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Dot extends AbsCoin {
    public static final Map<Integer, Pallet<? extends Parameter>> pallets = new HashMap<>();
    static {
        pallets.put(0x0500, new Transfer(Network.POLKADOT, 0x0500));
        pallets.put(0x0503, new TransferKeepAlive(Network.POLKADOT, 0x0503));
        pallets.put(0x0900, new SetKeys(Network.POLKADOT, 0x0900));
        //Staking
        pallets.put(0x0700, new Bond(Network.POLKADOT, 0x0700));
        pallets.put(0x0701, new BondExtra(Network.POLKADOT, 0x0701));
        pallets.put(0x0702, new Unbond(Network.POLKADOT, 0x0702));
        pallets.put(0x0703, new WithdrawUnbonded(Network.POLKADOT, 0x0703));
        pallets.put(0x0704, new Validate(Network.POLKADOT, 0x0704));
        pallets.put(0x0705, new Nominate(Network.POLKADOT, 0x0705));
        pallets.put(0x0706, new Chill(Network.POLKADOT, 0x0706));
        pallets.put(0x0707, new SetPayee(Network.POLKADOT, 0x0707));
        pallets.put(0x0708, new SetController(Network.POLKADOT, 0x0708));
        pallets.put(0x0709, new SetValidatorCount(Network.POLKADOT, 0x0709));
        pallets.put(0x070a, new IncreaseValidatorCount(Network.POLKADOT, 0x070a));
        pallets.put(0x070b, new ScaleValidatorCount(Network.POLKADOT, 0x070b));
        pallets.put(0x070c, new ForceNoEras(Network.POLKADOT, 0x070c));
        pallets.put(0x070d, new ForceNewEra(Network.POLKADOT, 0x070d));
        pallets.put(0x070e, new SetInvulnerables(Network.POLKADOT, 0x070e));
        pallets.put(0x070f, new ForceUnstake(Network.POLKADOT, 0x070f));
        pallets.put(0x0710, new ForceNewEraAlways(Network.POLKADOT, 0x0710));
        pallets.put(0x0711, new CancelDeferredSlash(Network.POLKADOT, 0x0711));
        pallets.put(0x0712, new PayoutStakers(Network.POLKADOT, 0x0712));
        pallets.put(0x0713, new Rebond(Network.POLKADOT, 0x0713));
        pallets.put(0x0714, new SetHistoryDepth(Network.POLKADOT, 0x0714));
        pallets.put(0x0715, new ReapStash(Network.POLKADOT, 0x0715));

        pallets.put(0x0e0b, new Delegate(Network.POLKADOT, 0x0e0b));

        pallets.put(0x1c01, new SetIdentity(Network.POLKADOT, 0x1c01));

        pallets.put(0x1d01, new AddProxy(Network.POLKADOT, 0x1d01));

        pallets.put(0x1a00, new Batch(Network.POLKADOT, 0x1a00));
        pallets.put(0x1a02, new BatchAll(Network.POLKADOT, 0x1a02));
        pallets.put(0x1100, new Vote(Network.POLKADOT, 0x1100));
    }

    public Dot(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.DOT.coinCode();
    }

    public static class Tx extends AbsTx {

        public Tx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
            super(object, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException {
            to = metaData.getString("dest");
            amount = metaData.getLong("value") / Math.pow(10, decimal);
            fee = metaData.optLong("tip",0) / Math.pow(10, decimal);

            if (!metaData.has("nonce")) {
                metaData.put("nonce",0);
            }
            if (!metaData.has("implVersion")) {
                metaData.put("implVersion",0);
            }
            if (!metaData.has("authoringVersion")) {
                metaData.put("authoringVersion",0);
            }
            metaData.put("eraPeriod",4096);

        }

        @Override
        protected void checkHdPath() throws InvalidTransactionException {
            Coins.Coin coin = Coins.SUPPORTED_COINS.stream()
                    .filter(c->c.coinCode().equals(coinCode))
                    .findFirst().orElse(null);

             if(coin == null || !hdPath.equals(coin.getAccounts()[0])) {
                 throw new InvalidTransactionException(String.format("invalid hdPath \"%s\" for %s", hdPath, coinCode));
             }
        }
    }

    public static class Deriver extends AbsDeriver {
        protected byte prefix = 0;
        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {
            byte[] bytes = new B58().decode(xPubKey);
            byte[] pubKey = Arrays.copyOfRange(bytes,bytes.length - 4 - 32,bytes.length - 4);
            return AddressCodec.encodeAddress(pubKey, prefix);
        }

        @Override
        public String derive(String xPubKey) {
            byte[] bytes = new B58().decode(xPubKey);
            byte[] pubKey = Arrays.copyOfRange(bytes,bytes.length - 4 - 32,bytes.length - 4);
            return AddressCodec.encodeAddress(pubKey, prefix);
        }
    }
}
