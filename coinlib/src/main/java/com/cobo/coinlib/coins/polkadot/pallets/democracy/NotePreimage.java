package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecReader;

public class NotePreimage extends Pallet<NotePreimageParameter> {
    public NotePreimage(Network network, int code) {
        super("democracy.notePreimage", network, code);
    }

    @Override
    public NotePreimageParameter read(ScaleCodecReader scr) {
        return new NotePreimageParameter(name, network, code, scr);
    }
}
