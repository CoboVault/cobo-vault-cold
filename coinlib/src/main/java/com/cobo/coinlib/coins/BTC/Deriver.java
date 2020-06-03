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

package com.cobo.coinlib.coins.BTC;

import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import static com.cobo.coinlib.coins.BTC.Btc.AddressType.P2PKH;
import static com.cobo.coinlib.coins.BTC.Btc.AddressType.P2SH;
import static com.cobo.coinlib.coins.BTC.Btc.AddressType.SegWit;

public class Deriver {

    protected DeterministicKey getAddrDeterministicKey(String accountXpub, int changeIndex, int addressIndex) {
        DeterministicKey account = DeterministicKey.deserializeB58(accountXpub, MainNetParams.get());
        DeterministicKey change = HDKeyDerivation.deriveChildKey(account, changeIndex);
        return HDKeyDerivation.deriveChildKey(change, addressIndex);
    }


    public String derive(String accountXpub,
                         int changeIndex,
                         int addressIndex,
                         Btc.AddressType type) {
        DeterministicKey address = getAddrDeterministicKey(accountXpub, changeIndex, addressIndex);

        if (type == P2PKH) {
            return LegacyAddress.fromPubKeyHash(MainNetParams.get(),address.getPubKeyHash())
                    .toBase58();
        } else if(type == SegWit) {
            return SegwitAddress.fromHash(MainNetParams.get(), address.getPubKeyHash()).toBech32();
        } else if (type == P2SH){
            return LegacyAddress.fromScriptHash(MainNetParams.get(),
                    segWitOutputScript(address.getPubKeyHash()).getPubKeyHash()).toBase58();
        } else {
            throw new IllegalArgumentException();
        }
    }

    protected Script segWitOutputScript(byte[] pubKeyHash) {
        return ScriptBuilder.createP2SHOutputScript(segWitRedeemScript(pubKeyHash));
    }

    private Script segWitRedeemScript(byte[] pubKeyHash) {
        return new ScriptBuilder().smallNum(0).data(pubKeyHash).build();
    }

}
