package com.cobo.coinlib.coins.DOT;

import com.cobo.coinlib.exception.InvalidUOSException;

import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class UOSDecoder {
    public static class Network {
        public String name;
        public byte SS58Prefix;
        public String genesisHash;

        public Network(String name, byte SS58Prefix, String genesisHash) {
            this.name = name;
            this.SS58Prefix = SS58Prefix;
            this.genesisHash = genesisHash;
        }
    }

    private static List<Network> networks = Arrays.asList(new Network("Polkadot", (byte) 0, "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3"), new Network("Kusama", (byte) 2, "b0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe"));
//    private static Map<String, String> palletMap = new HashMap<>();
//    static {
//        palletMap.put("0503", "transferKeepAlive");
//        palletMap.put("0500", "transfer");
//    }

    public class UOSDecodedTransaction {
        private byte[] destinationPublicKey;
        private String palletId;
        private BigInteger amount;
        private String era;
        private BigInteger nonce;
        private BigInteger tip;
        private long specVersion;
        private long transactionVersion;
        private String genesisHash;
        private String blockHash;
        private Network network;

        public UOSDecodedTransaction(byte[] destinationPublicKey, String palletId, BigInteger amount, String era, BigInteger nonce, BigInteger tip, long specVersion, long transactionVersion, String genesisHash, String blockHash) {
            this.network = networks.stream().filter(n -> n.genesisHash.equals(genesisHash)).findFirst().orElse(networks.get(0));
            this.destinationPublicKey = destinationPublicKey;
            this.palletId = palletId;
            this.amount = amount;
            this.era = era;
            this.nonce = nonce;
            this.tip = tip;
            this.specVersion = specVersion;
            this.transactionVersion = transactionVersion;
            this.genesisHash = genesisHash;
            this.blockHash = blockHash;
        }

        public String getDestination() {
            return AddressCodec.encodeAddress(destinationPublicKey, network.SS58Prefix);
        }

        public String getPalletId() {
            return palletId;
        }

        public String getAmount() {
            return amount.toString();
        }

        public String getEra() {
            return era;
        }

        public String getNonce() {
            return nonce.toString();
        }

        public String getTip() {
            return tip.toString();
        }

        public long getSpecVersion() {
            return specVersion;
        }

        public long getTransactionVersion() {
            return transactionVersion;
        }

        public String getGenesisHash() {
            return genesisHash;
        }

        public String getBlockHash() {
            return blockHash;
        }
    }

    public class UOSDecodeResult {
        public UOSDecodeResult(String curve, byte[] accountPublicKey, String genesisHash, boolean isOversize) {
            this.network = UOSDecoder.networks.stream().filter(n -> n.genesisHash.equals(genesisHash)).findFirst().orElse(networks.get(0));
            this.curve = curve;
            this.accountPublicKey = accountPublicKey;
            this.isOversize = isOversize;
            this.decodedTransaction = null;
        }

        public void setRawSigningPayload(byte[] rawSigngingPayload) {
            this.rawSigningPayload = rawSigngingPayload;
        }

        public void setDecodedTransaction(UOSDecodedTransaction decodedTransaction) {
            this.decodedTransaction = decodedTransaction;
        }

        public void setIsHash(boolean isHash) {
            isHash = isHash;
        }

        private String curve;
        private byte[] accountPublicKey;
        private Network network;
        private byte[] rawSigningPayload;
        private boolean isOversize;
        private boolean isHash;
        private UOSDecodedTransaction decodedTransaction;

        public String getCurve() {
            return curve;
        }

        public String getAccount() {
            return AddressCodec.encodeAddress(accountPublicKey, network.SS58Prefix);
        }

        public Network getNetwork() {
            return network;
        }

        public byte[] getSigningPayload() {
            return isHash ? rawSigningPayload : isOversize ? AddressCodec.blake2b(rawSigningPayload, 256) : rawSigningPayload;
        }

        public boolean isHash() {
            return isHash;
        }

        public UOSDecodedTransaction getDecodedTransaction() {
            return isHash ? null : decodedTransaction;
        }
    }

    public UOSDecoder() {
    }

    /* 000001000053010228b9ffce010cff941262f1b5fa5a884a65b2f7324854082abd68aa3d93b0827fa005038cba3d59242abc565c99a47c3afaf23668f2e1b1a76a38ab71868ae2dafca9630700e40b5402d500240284d717190000000500000091b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3 */
    public UOSDecodeResult decodeUOSRawData(String rawData, boolean multipartComplete) throws InvalidUOSException {
        String UOSRawData = this.extractUOSRawData(rawData);
        String frameInfo = UOSRawData.substring(0, 10);
        int frameCount = Utils.tryParseInt(frameInfo.substring(2, 6));
        boolean isMultiPart = frameCount > 1;
        if (frameCount > 50) {
            throw new InvalidUOSException("Frames number is too big, the QR seems not to be a recognized extrinsic raw data");
        }
        int currentFrame = Utils.tryParseInt(frameInfo.substring(6, 10));
        String uosAfterFrame = UOSRawData.substring(10);
        if (isMultiPart && !multipartComplete) {
            return null;
        }
        String zerothByte = uosAfterFrame.substring(0, 2);
        if ("53".equals(zerothByte)) {
            return this.dealWithSubstratePayload(uosAfterFrame.substring(2));
        }
        throw new InvalidUOSException("current not support ethereum and legacy ethereum payload");
    }

    private UOSDecodeResult dealWithSubstratePayload(String substrateFrame) throws InvalidUOSException {
        ScaleCodecReader scaleCodecReader = new ScaleCodecReader(Hex.decode(substrateFrame));
        byte firstByte = scaleCodecReader.readByte();
        byte secondByte = scaleCodecReader.readByte();
        String curve;
        switch (firstByte) {
            case 0x00:
                curve = "ed25519";
                break;
            case 0x01:
                curve = "sr25519";
                break;
            default:
                throw new InvalidUOSException("invalid curve bytes");
        }
        byte[] accountPublicKey = scaleCodecReader.readByteArray(32);
        String restString = scaleCodecReader.readRestString();
        String rawPayload = restString.substring(0, restString.length() - 64);
        boolean isOversize = restString.length() > 512;
        String genesisHash = restString.substring(restString.length() - 64);
        UOSDecodeResult result = new UOSDecodeResult(curve, accountPublicKey, genesisHash, isOversize);
        switch (secondByte) {
            case 0x00:
            case 0x02: {
                ScaleCodecReader tempReader = new ScaleCodecReader(Hex.decode(rawPayload));
                result.setIsHash(false);
                //drop head
                tempReader.readCompact();

                byte[] rawSigningData = tempReader.readRestBytes();
                ScaleCodecReader txReader = new ScaleCodecReader(rawSigningData);

                result.setRawSigningPayload(rawSigningData);

                String pallet = txReader.readString(2);
                byte[] destinationPublicKey = txReader.readByteArray(32);

                BigInteger amount = txReader.readCompact();
                String encodedEra = txReader.readString(2);
                BigInteger nonce = txReader.readCompact();
                BigInteger tip = txReader.readCompact();
                long specVersion = txReader.readUint32();
                long transactionVersion = txReader.readUint32();
                String chainGenesisHash = txReader.readString(32);
                String blockHash = txReader.readString(32);

                UOSDecodedTransaction decodedTransaction = new UOSDecodedTransaction(
                        destinationPublicKey,
                        pallet,
                        amount,
                        encodedEra,
                        nonce,
                        tip,
                        specVersion,
                        transactionVersion,
                        chainGenesisHash,
                        blockHash
                );
                result.setDecodedTransaction(decodedTransaction);
                break;
            }
            case 0x01: {
                result.setIsHash(true);
                result.setRawSigningPayload(Hex.decode(rawPayload));
                break;
            }
            default: {
                throw new InvalidUOSException("invalid data type byte");
            }
        }

        return result;
    }

    /* 400c0000001000053010228b9ffce010cff941262f1b5fa5a884a65b2f7324854082abd68aa3d93b0827fa005038cba3d59242abc565c99a47c3afaf23668f2e1b1a76a38ab71868ae2dafca9630700e40b5402d500240284d717190000000500000091b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3cec018d65a9ed1edc74c6f5f9caedac4818c65251f46047668eed3d350e692fb91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c30ec11ec11ec11ec11ec11ec11ec11ec11ec11ec11ec */
    private String extractUOSRawData(String QRRawData) throws InvalidUOSException {
        if (QRRawData.length() == 0) {
            throw new InvalidUOSException("QRCode raw data is none");
        }
        if (QRRawData.endsWith("ec")) {
            QRRawData = QRRawData.substring(0, QRRawData.length() - 2);
        }
        while (QRRawData.endsWith("ec11")) {
            QRRawData = QRRawData.substring(0, QRRawData.length() - 4);
        }
        if (!QRRawData.startsWith("4") || !QRRawData.endsWith("0")) {
            throw new InvalidUOSException("QRCode raw data is invalid");
        }
        QRRawData = QRRawData.substring(1, QRRawData.length() - 1);
        int length8 = Utils.tryParseInt(QRRawData.substring(0, 2));
        int length16 = Utils.tryParseInt(QRRawData.substring(0, 4));
        if (length8 * 2 + 2 == QRRawData.length()) {
            QRRawData = QRRawData.substring(2);
        } else if (length16 * 2 + 4 == QRRawData.length()) {
            QRRawData = QRRawData.substring(4);
        } else {
            throw new InvalidUOSException("QRCode raw data is invalid");
        }
        return QRRawData;
    }
}

class Utils {
    public static int tryParseInt(String intHexStr) {
        try {
            return Integer.parseInt(intHexStr, 16);
        } catch (Exception e) {
            return 0;
        }
    }

    public static BigInteger tryParseLEBigInt(String bigIntHexStr) {
        try {
            byte[] bytes = Hex.decode(bigIntHexStr);
            int len = bytes.length;
            int j = len - 1;
            byte[] reversedBytes = new byte[j];
            for (int i = 0; i < bytes.length; i++) {
                reversedBytes[j] = bytes[i];
                j--;
            }
            String reversedHex = Hex.toHexString(reversedBytes);
            return new BigInteger(reversedHex, 16);
        } catch (Exception e) {
            return new BigInteger("0");
        }
    }
}
