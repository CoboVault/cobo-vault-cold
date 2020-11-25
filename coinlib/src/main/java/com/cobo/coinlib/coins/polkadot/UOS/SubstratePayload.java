package com.cobo.coinlib.coins.polkadot.UOS;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.exception.InvalidUOSException;

import org.bouncycastle.util.encoders.Hex;

import java.util.Arrays;
import java.util.List;

public class SubstratePayload {
    private final String rawData;

    public String curve;
    public byte[] accountPublicKey;
    public boolean isHash;
    public boolean isOversize;
    public String genesisHash;
    public Extrinsic extrinsic;
    public byte[] rawSigningData;

    public Network network;

    public SubstratePayload(String rawData) throws InvalidUOSException {
        this.rawData = rawData;
        read();
    }

    private void read() throws InvalidUOSException {
        ScaleCodecReader scaleCodecReader = new ScaleCodecReader(Hex.decode(rawData));
        byte firstByte = scaleCodecReader.readByte();
        byte secondByte = scaleCodecReader.readByte();
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
        accountPublicKey = scaleCodecReader.readByteArray(32);
        String restString = scaleCodecReader.readRestString();
        String rawPayload = restString.substring(0, restString.length() - 64);
        isOversize = restString.length() > 512;
        genesisHash = restString.substring(restString.length() - 64);
        network = UOSDecoder.supportedNetworks.stream()
                .filter(n -> n.genesisHash.equals(genesisHash))
                .findFirst()
                .orElse(new Network("UNKNOWN", (byte) 0, genesisHash, 0));

        switch (secondByte){
            case 0x00:
            case 0x02: {
                isHash = false;
                ScaleCodecReader tempReader = new ScaleCodecReader(Hex.decode(rawPayload));
                tempReader.readCompact();
                rawSigningData = tempReader.readRestBytes();
                extrinsic = new Extrinsic(rawSigningData, network);
                break;
            }
            case 0x01: {
                isHash = true;
                rawSigningData = Hex.decode(rawPayload);
                break;
            }
            default: {
                throw new InvalidUOSException("invalid data type byte");
            }
        }
    }
}
