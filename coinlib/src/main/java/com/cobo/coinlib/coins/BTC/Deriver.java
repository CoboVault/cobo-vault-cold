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

import com.cobo.coinlib.ExtendPubkeyFormat;

import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.spongycastle.util.encoders.Hex;

import java.util.List;
import java.util.stream.Collectors;

import static com.cobo.coinlib.ExtendPubkeyFormat.convertExtendPubkey;
import static com.cobo.coinlib.coins.BTC.Btc.AddressType.P2PKH;
import static com.cobo.coinlib.coins.BTC.Btc.AddressType.P2SH;
import static com.cobo.coinlib.coins.BTC.Btc.AddressType.SegWit;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKMULTISIG;

public class Deriver {
    private NetworkParameters network;
    public Deriver(boolean isMainNet) {
        this.network = isMainNet ? MainNetParams.get() : TestNet3Params.get();
    }

    private DeterministicKey getAddrDeterministicKey(String accountXpub, int changeIndex, int addressIndex) {
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
            return LegacyAddress.fromPubKeyHash(network, address.getPubKeyHash()).toBase58();
        } else if(type == SegWit) {
            return SegwitAddress.fromHash(network, address.getPubKeyHash()).toBech32();
        } else if (type == P2SH) {
            return LegacyAddress.fromScriptHash(network,
                    segWitOutputScript(address.getPubKeyHash()).getPubKeyHash()).toBase58();
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Script segWitOutputScript(byte[] pubKeyHash) {
        return ScriptBuilder.createP2SHOutputScript(segWitRedeemScript(pubKeyHash));
    }

    private Script segWitRedeemScript(byte[] pubKeyHash) {
        return new ScriptBuilder().smallNum(0).data(pubKeyHash).build();
    }

    public String deriveMultiSigAddress(int threshold, List<String> xPubs,
                                               int[] path, Btc.AddressType type) {
        checkArgument(path.length == 2);
        checkArgument(path[0] == 0 || path[0] == 1);
        checkArgument(path[1] >= 0);

        List<byte[]> orderedPubKeys = xPubs.stream()
                .map(xpub -> convertExtendPubkey(xpub, ExtendPubkeyFormat.xpub))
                .map(xpub -> derivePublicKey(xpub,path[0],path[1]))
                .sorted()
                .map(Hex::decode)
                .collect(Collectors.toList());

        return createMultiSigAddress(threshold, orderedPubKeys, type);
    }

    private String derivePublicKey(String xpub, int change, int index) {
        DeterministicKey key = DeterministicKey.deserializeB58(xpub, network);
        DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(key, change);
        return HDKeyDerivation.deriveChildKey(changeKey, index).getPublicKeyAsHex();
    }


    public String createMultiSigAddress(int threshold,
                                               List<byte[]> pubKeys,
                                               Btc.AddressType type) {
        Script p2ms = createMultiSigOutputScript(threshold, pubKeys);
        Script p2wsh = ScriptBuilder.createP2WSHOutputScript(p2ms);

        if (type == SegWit) {
            return SegwitAddress.fromHash(network, p2wsh.getPubKeyHash()).toBech32();
        } else {
            Script p2sh = ScriptBuilder.createP2SHOutputScript(p2wsh);
            return LegacyAddress.fromScriptHash(network, p2sh.getPubKeyHash()).toBase58();
        }
    }

    private static Script createMultiSigOutputScript(int threshold, List<byte[]> pubKeys) {
        checkArgument(threshold > 0);
        checkArgument(threshold <= pubKeys.size());
        checkArgument(pubKeys.size() <= 15);
        ScriptBuilder builder = new ScriptBuilder();
        builder.smallNum(threshold);
        for (byte[] key : pubKeys) {
            builder.data(key);
        }
        builder.smallNum(pubKeys.size());
        builder.op(OP_CHECKMULTISIG);
        return builder.build();
    }

    private static void checkArgument(boolean b) {
        if (!b) throw new IllegalArgumentException();
    }

}
