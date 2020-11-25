package com.cobo.coinlib.coins.polkadot.UOS;

import com.cobo.coinlib.exception.InvalidUOSException;

import java.util.Arrays;
import java.util.List;

public class UOSDecoder {
    public static final Network POLKADOT = new Network("Polkadot", (byte) 0, "91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3", 10);
    public static final Network KUSAMA = new Network("Kusama", (byte) 2, "b0a8d493285c2df73290dfb7e61f870f17b41801197a149ca93654499ea3dafe", 12);

    public static final List<Network> supportedNetworks = Arrays.asList(
            POLKADOT, KUSAMA
    );

    public static Result decode(String rawData, boolean multipartComplete)
            throws InvalidUOSException {
        String UOSRawData = extractUOSRawData(rawData);
        MultipartPayload mp = new MultipartPayload(UOSRawData, multipartComplete);
        SubstratePayload sp = mp.substratePayload;

        Result result = new Result();

        result.setFrameCount(mp.frameCount);
        result.setCurrentFrame(mp.currentFrame);
        result.setMultiPart(mp.isMultiPart);

        result.setIsHash(sp.isHash);
        result.setAccountPublicKey(sp.accountPublicKey);
        result.setCurve(sp.curve);
        result.setNetwork(sp.network);
        result.setOversize(sp.isOversize);
        result.setExtrinsic(sp.extrinsic);
        result.setRawSigningPayload(sp.rawSigningData);

        return result;
    }

    private static String extractUOSRawData(String QRRawData) throws InvalidUOSException {
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
