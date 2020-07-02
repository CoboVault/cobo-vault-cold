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

package com.cobo.coinlib.coins.BTC.Electrum;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;

public class TransactionOutput {
    private Message txMessage;
    public long value;
    public String scriptPubKey;
    public String address;
    public NetworkParameters network;

    public TransactionOutput(Message txMessage, NetworkParameters network){
        this.network = network;
        this.txMessage = txMessage;
        this.value = parseValue();
        this.scriptPubKey = parseScriptPubKey();
        this.address = parseAddress();

    }

    private long parseValue() {
        return this.txMessage.readInt64();
    }

    private String parseScriptPubKey() {
        return Hex.toHexString(this.txMessage.readByteArray());
    }

    private String parseAddress(){
        Script script = new Script(Hex.decode(this.scriptPubKey));
        Address address = script.getToAddress(network);
        if (address instanceof LegacyAddress) {
            return ((LegacyAddress) address).toBase58();
        } else {
            return ((SegwitAddress) address).toBech32();
        }
    }


}
