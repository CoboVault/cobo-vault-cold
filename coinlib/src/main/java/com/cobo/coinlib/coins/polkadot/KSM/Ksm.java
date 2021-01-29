/*
 *
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
 *
 */

package com.cobo.coinlib.coins.polkadot.KSM;

import com.cobo.coinlib.coins.AbsDeriver;
import com.cobo.coinlib.coins.polkadot.AddressCodec;
import com.cobo.coinlib.coins.polkadot.DOT.Dot;
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

public class Ksm extends Dot {
    public static final Map<Integer, Pallet<? extends Parameter>> pallets = new HashMap<>();
    //fix code
    static {
        pallets.put(0x0400, new Transfer(Network.KUSAMA, 0x0400));
        pallets.put(0x0403, new TransferKeepAlive(Network.KUSAMA, 0x0403));
        pallets.put(0x0800, new SetKeys(Network.KUSAMA, 0x0800));
        //Staking
        pallets.put(0x0600, new Bond(Network.KUSAMA, 0x0600));
        pallets.put(0x0601, new BondExtra(Network.KUSAMA, 0x0601));
        pallets.put(0x0602, new Unbond(Network.KUSAMA, 0x0602));
        pallets.put(0x0603, new WithdrawUnbonded(Network.KUSAMA, 0x0603));
        pallets.put(0x0604, new Validate(Network.KUSAMA, 0x0604));
        pallets.put(0x0605, new Nominate(Network.KUSAMA, 0x0605));
        pallets.put(0x0606, new Chill(Network.KUSAMA, 0x0606));
        pallets.put(0x0607, new SetPayee(Network.KUSAMA, 0x0607));
        pallets.put(0x0608, new SetController(Network.KUSAMA, 0x0608));
        pallets.put(0x0609, new SetValidatorCount(Network.KUSAMA, 0x0609));
        pallets.put(0x060a, new IncreaseValidatorCount(Network.KUSAMA, 0x060a));
        pallets.put(0x060b, new ScaleValidatorCount(Network.KUSAMA, 0x060b));
        pallets.put(0x060c, new ForceNoEras(Network.KUSAMA, 0x060c));
        pallets.put(0x060d, new ForceNewEra(Network.KUSAMA, 0x060d));
        pallets.put(0x060e, new SetInvulnerables(Network.KUSAMA, 0x060e));
        pallets.put(0x060f, new ForceUnstake(Network.KUSAMA, 0x060f));
        pallets.put(0x0610, new ForceNewEraAlways(Network.KUSAMA, 0x0610));
        pallets.put(0x0611, new CancelDeferredSlash(Network.KUSAMA, 0x0611));
        pallets.put(0x0612, new PayoutStakers(Network.KUSAMA, 0x0612));
        pallets.put(0x0613, new Rebond(Network.KUSAMA, 0x0613));
        pallets.put(0x0614, new SetHistoryDepth(Network.KUSAMA, 0x0614));
        pallets.put(0x0615, new ReapStash(Network.KUSAMA, 0x0615));

        pallets.put(0x0d0b, new Delegate(Network.KUSAMA, 0x0d0b));

        pallets.put(0x1901, new SetIdentity(Network.KUSAMA, 0x1901));

        pallets.put(0x1e01, new AddProxy(Network.KUSAMA, 0x1e01));

        pallets.put(0x1800, new Batch(Network.KUSAMA, 0x1800));
        pallets.put(0x1802, new BatchAll(Network.KUSAMA, 0x1802));
        pallets.put(0x1000, new Vote(Network.KUSAMA, 0x1000));
    }

    public Ksm(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.KSM.coinCode();
    }

    public static class Tx extends Dot.Tx {

        public Tx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
            super(object, coinCode);
        }
    }

    public static class Deriver extends AbsDeriver {
        protected byte prefix = 2;
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
