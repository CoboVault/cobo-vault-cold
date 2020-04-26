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

package com.cobo.coinlib.v8;

import com.cobo.coinlib.utils.HDKey;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8TypedArray;

import org.bouncycastle.util.encoders.Hex;

public class HDKeyImpl extends HDKey {

    private V8 v8;
    private V8Function getPublicKeyFunction;
    private V8Function derivePublicKeyFunction;

    private final String nameSpace = "cryptoCoinKit.utils.";

    @Override
    public String getPublicKey(String accountExtendPublicKey) {
        checkAndInit();
        if (getPublicKeyFunction == null) {
            getPublicKeyFunction = (V8Function) v8.executeObjectScript(nameSpace + "xpubToPubkey");
        }

        V8Array params = new V8Array(v8);
        params.push(accountExtendPublicKey);

        V8TypedArray pubkeyBuffer = (V8TypedArray) getPublicKeyFunction.call(v8, params);
        String publicKey = Hex.toHexString(pubkeyBuffer.getBytes(0, pubkeyBuffer.length()));
        params.close();
        pubkeyBuffer.close();
        getPublicKeyFunction.close();
        v8.close();
        return publicKey;
    }

    @Override
    public String derivePublicKey(String accountExtendPublicKey, int changeIndex, int index) {
        checkAndInit();
        if (derivePublicKeyFunction == null) {
            derivePublicKeyFunction = (V8Function) v8.executeObjectScript(nameSpace + "xpubToPubkey");
        }
        V8Array params = new V8Array(v8);
        V8Array derivePath = new V8Array(v8);
        params.push(accountExtendPublicKey).
                push(derivePath.push(changeIndex).push(index));

        V8TypedArray pubkeyBuffer = (V8TypedArray) derivePublicKeyFunction.call(v8, params);
        String publicKey = Hex.toHexString(pubkeyBuffer.getBytes(0, pubkeyBuffer.length()));
        params.close();
        pubkeyBuffer.close();
        derivePath.close();
        derivePublicKeyFunction.close();
        v8.close();
        return publicKey;
    }

    private void checkAndInit() {
        if (v8 == null || v8.isReleased()) {
            v8 = ScriptLoader.sInstance.loadByFileName("utils.bundle.js");
        }
    }
}
