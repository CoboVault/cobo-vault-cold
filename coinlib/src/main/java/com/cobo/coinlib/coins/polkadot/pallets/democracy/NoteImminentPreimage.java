package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Pallet;

public class NoteImminentPreimage extends Pallet<NotePreimageParameter> {
    public NoteImminentPreimage(Network network, int code) {
        super("democracy.noteImminentPreimage", network, code);
    }

    @Override
    public NotePreimageParameter read(ScaleCodecReader scr) {
        return new NotePreimageParameter(name, network, code, scr);
    }
}
