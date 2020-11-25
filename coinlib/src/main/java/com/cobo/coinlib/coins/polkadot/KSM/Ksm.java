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
import com.cobo.coinlib.coins.polkadot.pallets.balance.Transfer;
import com.cobo.coinlib.coins.polkadot.pallets.balance.TransferKeepAlive;
import com.cobo.coinlib.coins.polkadot.pallets.elections_phragmen.Vote;
import com.cobo.coinlib.coins.polkadot.pallets.session.SetKeys;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Bond;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Nominate;
import com.cobo.coinlib.coins.polkadot.pallets.staking.SetController;
import com.cobo.coinlib.coins.polkadot.pallets.staking.Validate;
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
    public static final Map<Integer, Pallet> pallets = new HashMap<>();
    //fix code
    static {
        pallets.put(0x0400, new Transfer(Network.KUSAMA));
        pallets.put(0x0403, new TransferKeepAlive(Network.KUSAMA));
        pallets.put(0x0800, new SetKeys(Network.KUSAMA));
        pallets.put(0x0600, new Bond(Network.KUSAMA));
        pallets.put(0x0604, new Validate(Network.KUSAMA));
        pallets.put(0x0605, new Nominate(Network.KUSAMA));
        pallets.put(0x0608, new SetController(Network.KUSAMA));
        pallets.put(0x1800, new Batch(Network.KUSAMA));
        pallets.put(0x1802, new BatchAll(Network.KUSAMA));
        pallets.put(0x1000, new Vote(Network.KUSAMA));
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
