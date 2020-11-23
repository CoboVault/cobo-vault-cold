package com.cobo.coinlib.coin;

import com.cobo.coinlib.coins.polkadot.UOS.Extrinsic;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.UOS.Result;
import com.cobo.coinlib.coins.polkadot.UOS.UOSDecoder;
import com.cobo.coinlib.coins.polkadot.pallets.balance.TransferKeepAlive;
import com.cobo.coinlib.coins.polkadot.pallets.balance.TransferParameter;
import com.cobo.coinlib.exception.InvalidUOSException;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class UOSDecoderTest {

    @Test
    public void testDecodeUOSRawData() throws InvalidUOSException {
        String UOSRawData = "400c0000001000053010228b9ffce010cff941262f1b5fa5a884a65b2f7324854082abd68aa3d93b0827fa005038cba3d59242abc565c99a47c3afaf23668f2e1b1a76a38ab71868ae2dafca9630700e40b5402d500240284d717190000000500000091b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c30ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec";
        Result result = UOSDecoder.decode(UOSRawData, false);
        assertEquals(result.frameCount, 1);
        assertEquals(result.currentFrame, 0);
        assertEquals(result.isMultiPart, false);

        assertEquals(result.curve, "sr25519");
        assertEquals(result.isHash, false);
        assertEquals(result.getAccount(), "1vQAnWwUYeEnoF1yK51ZmHpaVRs6inHHEJhzJto3xgqe4pF");
        Network network  = result.network;
        assertEquals(network.SS58Prefix, (byte) 0);
        assertEquals(network.genesisHash, "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3");
        assertEquals(network.name, "Polkadot");

        assertArrayEquals(result.getSigningPayload(), Hex.decode("0503" +
                "8cba3d59242abc565c99a47c3afaf23668f2e1b1a76a38ab71868ae2dafca963" +
                "0700e40b5402" +
                "d500" +
                "24" +
                "0284d717" +
                "1900000005000000" +
                "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3" +
                "cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb"));

        Extrinsic extrinsic = result.extrinsic;

        assertEquals(extrinsic.palletParameter.name, "balance.transferKeepAlive");
        TransferParameter tp = (TransferParameter) extrinsic.palletParameter;

        assertEquals(tp.getDestination(), "14BX2fAup13B79jAJhHDfrkNitWBXV6Fc6dYKjrsNmb8Fo7F");
        assertEquals(tp.getAmount(), "1");

        assertEquals(extrinsic.getEra(), "d500");
        assertEquals(extrinsic.getNonce(), "9");
        assertEquals(extrinsic.getTip(), "0.01");
        assertEquals(extrinsic.getSpecVersion(), 25);
        assertEquals(extrinsic.getTransactionVersion(), 5);
        assertEquals(extrinsic.getGenesisHash(), "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3");
        assertEquals(extrinsic.getBlockHash(), "cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb");
    }
}
