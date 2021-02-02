package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class NoteImminentPreimageOperational extends Pallet<NotePreimageParameter> {
    public NoteImminentPreimageOperational(Network network, int code) {
        super("democracy.noteImminentPreimageOperational", network, code);
    }

    @Override
    public NotePreimageParameter read(ScaleCodecReader scr) {
        return new NotePreimageParameter(name, network, code, scr);
    }
}
