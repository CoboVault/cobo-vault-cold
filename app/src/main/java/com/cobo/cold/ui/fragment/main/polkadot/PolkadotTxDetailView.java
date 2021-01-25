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

package com.cobo.cold.ui.fragment.main.polkadot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ScrollView;

import androidx.databinding.DataBindingUtil;

import com.cobo.coinlib.utils.Coins;
import com.cobo.cold.R;
import com.cobo.cold.databinding.ParamItemBinding;
import com.cobo.cold.databinding.PolkadotTxDetailBinding;
import com.cobo.cold.db.entity.TxEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class PolkadotTxDetailView extends ScrollView {
    private final LayoutInflater inflater;
    private PolkadotTxDetailBinding mBinding;
    private JSONObject parameter;

    public PolkadotTxDetailView(Context context) {
        this(context, null);
    }

    public PolkadotTxDetailView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolkadotTxDetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public PolkadotTxDetailView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        inflater = LayoutInflater.from(context);
    }

    public void setData(TxEntity txEntity) {
        updateUI(txEntity);
    }

    private void updateUI(TxEntity txEntity) {
        mBinding = DataBindingUtil.getBinding(this);
        Objects.requireNonNull(mBinding).setTx(txEntity);
        try {
            parameter = new JSONObject(txEntity.getSignedHex());
            String[] moduleCall = parameter.getString("name").split("\\.");
            String module = moduleCall[0];
            String call = moduleCall[1];
            mBinding.module.setText(module);
            mBinding.call.setText(call);
            mBinding.network.setText(Coins.coinNameFromCoinCode(txEntity.getCoinCode()));
            boolean isBatch = moduleCall[0].equals("utility")
                    && (moduleCall[1].equals("batch") || moduleCall[1].equals("batchAll"));
            if (isBatch) {
                renderBatchCall();
            } else {
                renderCallArgs(parameter.getJSONObject("parameter"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void renderCallArgs(JSONObject callArgs) {
        Map<String, Object> paramMap = toMap(callArgs);
        for (String key : paramMap.keySet()) {
            Object value = paramMap.get(key);
            addParamItem(key, value.toString());
        }
    }

    private void renderBatchCall() throws JSONException {
        JSONArray paramArray = parameter.getJSONObject("parameter").getJSONArray("Calls");
        for (int i = 0; i < paramArray.length(); i++) {
            JSONObject param = paramArray.getJSONObject(i);
            String module = param.getString("name").split("\\.")[0];
            String call = param.getString("name").split("\\.")[1];
            addParamItem("Module", module);
            addParamItem("Call", call);
            renderCallArgs(param.getJSONObject("parameter"));
            addDivider();
        }
    }

    private void addDivider() {
        @SuppressLint("InflateParams")
        View div = inflater.inflate(R.layout.divider, null, false);
        mBinding.container.addView(div);
    }

    private void addParamItem(String key, String value) {
        ParamItemBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.param_item, null, false);
        binding.key.setText(key);
        binding.value.setText(value);
        mBinding.container.addView(binding.getRoot());
    }

    private Map<String, Object> toMap(JSONObject jsonObj) {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObj.keys();
        try {
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObj.get(key);
                map.put(key, value);
            }
            return map;
        } catch (JSONException ignored) {

        }
        return null;
    }
}
