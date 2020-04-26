package com.cobo.coinlib.coin;

import com.cobo.coinlib.coins.BTC.Electrum.ElectrumTx;
import com.cobo.coinlib.coins.BTC.Electrum.TxUtils;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElectrumTxTest {
    @Test
    public void testParseTxSignalInput() throws ElectrumTx.SerializationException {
        String electrumTxHex = "45505446ff0002000000000101bbd9dd3404ffd195aca4575bd850755a76dd33e0673442e948571d8b7e936ddb0000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff01a1eb00000000000017a914c4b3e26f1d2851ce097cc8943cc8d4c19d6202c387feffffffff27ec00000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b8000001004a830900";
        byte[] txBytes = Hex.decode(electrumTxHex);
        ElectrumTx tx = ElectrumTx.parse(txBytes);
        assertEquals(2, tx.getVersion());
        assertTrue(tx.isPartial());
        assertTrue(tx.isSegwit());
        assertEquals(1, tx.getInputs().size());
        assertEquals("p2wpkh-p2sh", tx.getInputs().get(0).type);
        assertEquals(60455, tx.getInputs().get(0).value.intValue());
        assertEquals("02a9bcccbd7b9292aa4346ef973a7135473571a79f95ea2ef2466da2f43ba76f66", tx.getInputs().get(0).pubKey.pubkey);
        assertEquals(1, tx.getOutputs().size());
        assertEquals("3Kd5rjiLtvpHv5nhYQNTTeRLgrz4om32PJ", tx.getOutputs().get(0).address);
    }


    @Test
    public void testParseTxMultiInputs() throws ElectrumTx.SerializationException {
        String electrumTxHex = "45505446ff0002000000000106bf345375d61738afe374872e92de093cbbcbc56494822186591772de26b91104000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffffbf345375d61738afe374872e92de093cbbcbc56494822186591772de26b911040100000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff023c272923e8abc0d8edd491d6986cec5d5c28204490bd55654122fc3abf2914000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffff44ba6e8c09775353eb6ef2b3b25aaeaaa6b48741671bb7fd0cdcd1b0aab1d731000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffff595bd8050cd602c686849c636afe7684c044f67297b404c9fa390f5f275f35a1000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffff5e730b6805a00247d5d140b7e89efaa983978963d42310fbb198ec961e7cd5e4000000001716001494cb85b5f8418d4c6b3dacdcd964ffaa2d104756fdffffff013ccb00000000000017a914c4b3e26f1d2851ce097cc8943cc8d4c19d6202c387feffffffff1b0800000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000000feffffffff788f00000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100fefffffffff00700000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000000feffffffff220800000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000000feffffffff220800000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000000feffffffff552b00000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b80000000056880900";
        byte[] txBytes = Hex.decode(electrumTxHex);
        ElectrumTx tx = ElectrumTx.parse(txBytes);
        assertEquals(2, tx.getVersion());
        assertTrue(tx.isPartial());
        assertTrue(tx.isSegwit());
        assertEquals(6, tx.getInputs().size());
        assertEquals("p2wpkh-p2sh", tx.getInputs().get(0).type);
        assertEquals("02057656d1036539463e925e9f7f8232120750667b77cde62dcaa31d3011d65c67", tx.getInputs().get(0).pubKey.pubkey);
        assertEquals("02a9bcccbd7b9292aa4346ef973a7135473571a79f95ea2ef2466da2f43ba76f66", tx.getInputs().get(1).pubKey.pubkey);
        assertEquals("02057656d1036539463e925e9f7f8232120750667b77cde62dcaa31d3011d65c67", tx.getInputs().get(5).pubKey.pubkey);
        assertEquals(36728, tx.getInputs().get(1).value.intValue());
        assertEquals(1, tx.getOutputs().size());
        assertEquals("3Kd5rjiLtvpHv5nhYQNTTeRLgrz4om32PJ", tx.getOutputs().get(0).address);
    }

    @Test
    public void testTxUtils() throws ElectrumTx.SerializationException {
        String rawHex = "ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100";
        TxUtils.PubKeyInfo pubKeyInfo = TxUtils.getPubKeyInfo(rawHex);
        assertEquals("xpub6D3i46Y43SFfjEBYheBK3btYMRm9Cfb8Tt4M5Bv16tArNBw5ATNyJWjdcMyLxoCdHWTvm3ak7j2BWacq5Lw478aYUeARoYm4dvaQgJBAGsb", pubKeyInfo.xpub);
        assertEquals("M/49'/0'/0'/0/1", pubKeyInfo.hdPath);
    }

    @Test
    public void testTx() throws ElectrumTx.SerializationException {
        String electrumTxHex = "45505446ff00020000000001065de964445cda78e446ced48cc775b989566e68ab3ec98f838b70c7be15ef35060000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff0450946ed5608cce509adf9a99bc480cf4a340326ab4ebc39e0f8650e64c580e010000001716001436def5f1b1a236723fd8892ec7a731666dcb6f7afdffffff4ea96eb5a19fe0187227cb1e8a6eb235bdb789e113ee427986e5aad9a38a7a200000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff8d726b19ff9a5d26bc3a2de5003f335538a7143f4b4e3d1d56d6bd6de13bfd780100000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff299c3c2890d4e9f304309e3510673809c60551a3fe30d74f51eb7d0d1747ffbc0000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff4e183bfc94644d331e5b1993a04d6effcf6d24113969ab841f497b76016b30f00000000017160014f1951325a309dc91a5831092eff2887b449f42d9fdffffff01204e00000000000017a914c4b3e26f1d2851ce097cc8943cc8d4c19d6202c387feffffffff220200000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100feffffffffc50e00000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b801000d00feffffffffe80300000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100feffffffffca0300000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100feffffffff580200000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b800000100feffffffff0c3600000000000000000201ff53ff049d7cb203bddc6f0d80000000cd01dd05ef6eae22b5c2156fc38da40b925c62e5677352e6a9f9c2212b6dd45f0239c284493a8bf05e0723f001634fac452f6289e20c496f9bfcbf83917972f3b80000010098900900";
        byte[] txBytes = Hex.decode(electrumTxHex);
        ElectrumTx tx = ElectrumTx.parse(txBytes);

        System.out.println(tx);
    }
}
