package com.cobo.coinlib.coins.TCFX;
import android.text.TextUtils;
import com.cobo.coinlib.coins.AbsCoin;
import com.cobo.coinlib.coins.AbsDeriver;
import com.cobo.coinlib.coins.AbsTx;
import com.cobo.coinlib.exception.InvalidTransactionException;
import com.cobo.coinlib.interfaces.Coin;
import com.cobo.coinlib.utils.Coins;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.DeterministicKey;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Arrays;

import static com.cobo.coinlib.Util.cleanHexPrefix;
import static com.cobo.coinlib.Util.sha3String;


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





public class Tcfx extends AbsCoin implements Coin {

    public Tcfx(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.TCFX.coinCode();
    }

    public static class Tx extends AbsTx {

        public Tx(JSONObject metaData, String coinCode) throws JSONException, InvalidTransactionException {
            super(metaData, coinCode);
        }

        private int tokenDecimal;

        @Override
        protected void parseMetaData() throws InvalidTransactionException {
            try {
                fee = calculateDisplayFee();
                if (metaData.has("override")) {
                    JSONObject override = metaData.getJSONObject("override");
                    tokenDecimal = override.optInt("decimals", decimal);
                    isToken = true;
                    tokenName = override.optString("tokenShortName",
                            metaData.optString("tokenFullName", coinCode));
                    String contractAddress = override.optString("contractAddress");
                    if (TextUtils.isEmpty(contractAddress)) {
                        throw new InvalidTransactionException("invalid contractAddress");
                    }
                    metaData.put("contractAddress", contractAddress);
                }
                to = metaData.getString("to");
                amount = calculateDisplayAmount();
                memo = metaData.optString("memo");
                boolean enableMemo = false;
                if (!TextUtils.isEmpty(memo) && !enableMemo) {
                    metaData.put("memo", "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public double getAmount() {
            if (isToken) {
                return getAmountWithoutFee();
            } else {
                return super.getAmount();
            }
        }

        private double calculateDisplayFee() throws JSONException {
            BigDecimal gasPrice = new BigDecimal(metaData.getString("gasPrice"));
            BigDecimal gasLimit = new BigDecimal(metaData.getString("gas"));

            return gasLimit.multiply(gasPrice)
                    .divide(BigDecimal.TEN.pow(decimal), Math.min(decimal, 8), BigDecimal.ROUND_HALF_UP).doubleValue();
        }

        private double calculateDisplayAmount() throws JSONException {

            int decimal = isToken ? tokenDecimal : this.decimal;
            String amount = metaData.getString("value");
            BigDecimal value = new BigDecimal(amount);
            return value.divide(BigDecimal.TEN.pow(decimal), Math.min(decimal, 8), BigDecimal.ROUND_HALF_UP).doubleValue();
        }

    }

    public static class Deriver extends AbsDeriver {
        @Override
        public String derive(String accountXpub, int changeIndex, int addrIndex) {
            DeterministicKey address = getAddrDeterministicKey(accountXpub, changeIndex, addrIndex);
            //decompress
            ECKey eckey = ECKey.fromPublicOnly(address.getPubKeyPoint());
            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);

            String s = Hex.toHexString(getAddress(hash));

            StringBuilder result = new StringBuilder(s.length() + 2);
            result.append("0x").append(s);
            return  result.toString();
        }

        @Override
        public String derive(String xPubKey) {
            DeterministicKey key = getDeterministicKey(xPubKey);
            //decompress
            ECKey eckey = ECKey.fromPublicOnly(key.getPubKey());

            byte[] pubKey = eckey.decompress().getPubKey();
            byte[] hash = new byte[pubKey.length - 1];
            System.arraycopy(pubKey, 1, hash, 0, hash.length);

            String s = Hex.toHexString(getAddress(hash));
            StringBuilder result = new StringBuilder(s.length() + 2);
            result.append("0x").append(s);
            return  result.toString();
        }

        public static byte[] getAddress(byte[] publicKey) {
            byte[] hash = new Keccak.Digest256().digest(publicKey);
            byte[] buffer = Arrays.copyOfRange(hash, hash.length - 20, hash.length);  // right most 160 bits
            buffer[0] = (byte)(buffer[0] & (byte)0x0f | (byte)0x10);
            return buffer;
        }

        public static String toChecksumAddress(String address) {
            String lowercaseAddress = cleanHexPrefix(address).toLowerCase();
            String addressHash = cleanHexPrefix(sha3String(lowercaseAddress));

            StringBuilder result = new StringBuilder(lowercaseAddress.length() + 2);

            result.append("0x");

            for (int i = 0; i < lowercaseAddress.length(); i++) {
                if (Integer.parseInt(String.valueOf(addressHash.charAt(i)), 16) >= 8) {
                    result.append(String.valueOf(lowercaseAddress.charAt(i)).toUpperCase());
                } else {
                    result.append(lowercaseAddress.charAt(i));
                }
            }

            return result.toString();
        }
    }
}
