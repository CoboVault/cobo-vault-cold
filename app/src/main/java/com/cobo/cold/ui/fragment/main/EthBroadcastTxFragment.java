/*
 *
 *  Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.cobo.cold.ui.fragment.main;

import android.os.Bundle;
import android.view.View;

import com.cobo.cold.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

public class EthBroadcastTxFragment extends BroadcastTxFragment {
    @Override
    protected int setView() {
        return R.layout.broadcast_tx_fragment;
    }

    @Override
    protected void init(View view) {
        super.init(view);
        mBinding.toolbar.setNavigationOnClickListener(v -> popBackStack(R.id.assetFragment,false));
        mBinding.broadcastHint.setText(R.string.sync_with_metamask);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public String getSignedTxData() {
        try {
            JSONObject signed = new JSONObject(txEntity.getSignedHex());
            signed.remove("abi");
            signed.remove("chainId");
            return Hex.toHexString(signed.toString().getBytes(StandardCharsets.UTF_8));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }
}
