package com.cobo.coinlib.coin;

import com.cobo.coinlib.coins.DOT.DotImpl;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DotImplTest {

    @Test
    public void testConstructTransaction() {
        DotImpl dot = new DotImpl("DOT");
        DotImpl.SubstrateTransactionInfo substrateTransactionInfo = new DotImpl.SubstrateTransactionInfo();
        substrateTransactionInfo.setAmount(10000000000L);
        substrateTransactionInfo.setBlockNumber(1517092);
        substrateTransactionInfo.setValidityPeriod(4096);
        substrateTransactionInfo.setSpecVersion(19);
        substrateTransactionInfo.setTransactionVersion(5);
        substrateTransactionInfo.setTip(100000000L);
        substrateTransactionInfo.setNonce(19);
        substrateTransactionInfo.setDest("14BX2fAup13B79jAJhHDfrkNitWBXV6Fc6dYKjrsNmb8Fo7F");
        substrateTransactionInfo.setBlockHash("cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb");
        try {
            byte[] result = dot.constructTransaction(substrateTransactionInfo);
            assertArrayEquals(result, Hex.decode(
                    "0503" +
                            "8cba3d59242abc565c99a47c3afaf23668f2e1b1a76a38ab71868ae2dafca963" +
                            "0700e40b5402" +
                            "4b62" +
                            "4c" +
                            "0284d717" +
                            "13000000" +
                            "05000000" +
                            "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3" +
                            "cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb"));
        } catch (Exception e) {

        }
    }


}
