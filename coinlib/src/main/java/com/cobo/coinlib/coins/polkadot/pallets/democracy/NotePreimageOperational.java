package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class NotePreimageOperational extends Pallet<NotePreimageParameter> {
    public NotePreimageOperational(Network network, int code) {
        super("democracy.notePreimageOperational", network, code);
    }

    @Override
    public NotePreimageParameter read(ScaleCodecReader scr) {
        return new NotePreimageParameter(name, network, code, scr);
    }
}
