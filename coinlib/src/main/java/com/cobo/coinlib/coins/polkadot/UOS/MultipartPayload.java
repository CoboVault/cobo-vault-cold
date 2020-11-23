package com.cobo.coinlib.coins.polkadot.UOS;

import com.cobo.coinlib.exception.InvalidUOSException;

public class MultipartPayload {
    private final String rawData;

    public int frameCount;
    public boolean isMultiPart;
    public int currentFrame;

    public SubstratePayload substratePayload;

    public MultipartPayload(String rawData, boolean multipartComplete) throws InvalidUOSException {
        this.rawData = rawData;
        read(multipartComplete);
    }

    private void read(boolean multipartComplete) throws InvalidUOSException {
        String frameInfo = rawData.substring(0, 10);
        frameCount = Utils.tryParseInt(frameInfo.substring(2, 6));
        isMultiPart = frameCount > 1;
        if(frameCount > 50) {
            throw new InvalidUOSException("Frames number is too big, the QR seems not to be a recognized extrinsic raw data");
        }
        currentFrame = Utils.tryParseInt(frameInfo.substring(6, 10));
        if(isMultiPart  && !multipartComplete) {
            return;
        }
        String uosAfterFrame = rawData.substring(10);
        String zerothByte = uosAfterFrame.substring(0, 2);
        if ("53".equals(zerothByte)) {
            substratePayload = new SubstratePayload(uosAfterFrame.substring(2));
            return;
        }
        throw new InvalidUOSException("current not support ethereum and legacy ethereum payload");
    }
}
