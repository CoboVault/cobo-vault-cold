package com.cobo.coinlib.coins.polkadot.UOS;

public class Network {
    public String name;
    public byte SS58Prefix;
    public String genesisHash;
    public int decimals;

    public Network(String name, byte SS58Prefix, String genesisHash, int decimals) {
        this.name = name;
        this.SS58Prefix = SS58Prefix;
        this.genesisHash = genesisHash;
        this.decimals = decimals;
    }
}
