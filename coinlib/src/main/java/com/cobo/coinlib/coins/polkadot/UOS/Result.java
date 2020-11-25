package com.cobo.coinlib.coins.polkadot.UOS;

import com.cobo.coinlib.coins.polkadot.AddressCodec;

public class Result {

    public String curve;
    private byte[] accountPublicKey;
    public Network network;
    private byte[] rawSigningPayload;
    public boolean isOversize;
    public boolean isHash;
    public Extrinsic extrinsic;

    public int frameCount;
    public int currentFrame;
    public boolean isMultiPart;

    public Result(){};

    public void setHash(boolean hash) {
        isHash = hash;
    }

    public void setExtrinsic(Extrinsic extrinsic) {
        this.extrinsic = extrinsic;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }


    public void setMultiPart(boolean isMultiPart) {
        this.isMultiPart = isMultiPart;
    }

    public boolean isOversize() {
        return isOversize;
    }

    public void setCurve(String curve) {
        this.curve = curve;
    }

    public void setAccountPublicKey(byte[] accountPublicKey) {
        this.accountPublicKey = accountPublicKey;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public void setOversize(boolean oversize) {
        isOversize = oversize;
    }

    public void setRawSigningPayload(byte[] rawSigningPayload) {
        this.rawSigningPayload = rawSigningPayload;
    }

    public void setIsHash(boolean isHash) {
        this.isHash = isHash;
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

    public Extrinsic getExtrinsic() {
        return extrinsic;
    }
}
