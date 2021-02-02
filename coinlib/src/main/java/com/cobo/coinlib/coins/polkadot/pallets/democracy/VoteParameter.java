package com.cobo.coinlib.coins.polkadot.pallets.democracy;

import com.cobo.coinlib.coins.polkadot.ScaleCodecReader;
import com.cobo.coinlib.coins.polkadot.UOS.Network;
import com.cobo.coinlib.coins.polkadot.pallets.Parameter;
import com.cobo.coinlib.coins.polkadot.pallets.Utils;
import com.cobo.coinlib.coins.polkadot.scale.ScaleCodecWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;

public class VoteParameter extends Parameter {
    private abstract static class Vote {
        protected abstract String getTypeName();

        protected abstract void write(ScaleCodecWriter scw) throws IOException;

        protected abstract void read(ScaleCodecReader scr);

        protected abstract JSONObject toJSON() throws JSONException;
    }

    private class StandardVote extends Vote {
        private boolean aye;
        private byte conviction;
        private BigInteger balance;

        public String getTypeName() {
            return "Standard";
        }

        @Override
        protected void write(ScaleCodecWriter scw) throws IOException {
            scw.writeBoolean(aye);
            scw.writeByte(conviction);
            scw.writeBIntCompact(balance);
        }

        @Override
        protected void read(ScaleCodecReader scr) {
            aye = scr.readBoolean();
            conviction = scr.readByte();
            balance = scr.readCompact();
        }

        @Override
        protected JSONObject toJSON() throws JSONException {
            return new JSONObject().put("Aye", aye)
                    .put("Conviction", Utils.transformConviction(conviction))
                    .put("Balance", Utils.getReadableBalanceString(network, balance));
        }
    }

    private class SplitVote extends Vote {
        private BigInteger aye;
        private BigInteger nay;

        public String getTypeName() {
            return "Split";
        }

        @Override
        protected void write(ScaleCodecWriter scw) throws IOException {
            scw.writeBIntCompact(aye);
            scw.writeBIntCompact(nay);
        }

        @Override
        protected void read(ScaleCodecReader scr) {
            aye = scr.readCompact();
            nay = scr.readCompact();
        }

        @Override
        protected JSONObject toJSON() throws JSONException {
            return new JSONObject().put("Aye", Utils.getReadableBalanceString(network, aye))
                    .put("Nay", Utils.getReadableBalanceString(network, nay));
        }
    }

    private long refIndex;
    private byte voteType;
    private Vote vote;

    public VoteParameter(String name, Network network, int code, ScaleCodecReader scr) {
        super(name, network, code, scr);
    }

    @Override
    protected void read(ScaleCodecReader scr) {
        refIndex = scr.readCompact().longValue();
        voteType = scr.readByte();
        if (voteType == 0x00) {
            vote = new StandardVote();
        } else {
            vote = new SplitVote();
        }
        vote.read(scr);
    }

    @Override
    protected void write(ScaleCodecWriter scw) throws IOException {
        scw.writeLIntCompact(refIndex);
        scw.writeByte(voteType);
        vote.write(scw);
    }

    @Override
    protected JSONObject addCallParameter() throws JSONException {
        return new JSONObject().put("RefIndex", refIndex).put("VoteType", vote.getTypeName())
                .put("Vote", vote.toJSON());
    }
}
