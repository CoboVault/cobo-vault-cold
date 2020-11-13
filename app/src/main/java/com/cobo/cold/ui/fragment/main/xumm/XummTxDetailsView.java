
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

package com.cobo.cold.ui.fragment.main.xumm;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.cobo.cold.R;
import com.cobo.cold.databinding.XrpTxItemBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XummTxDetailsView extends LinearLayout {
    private AppCompatActivity context;
    private List<String> sortedKeys;
    public XummTxDetailsView(Context context) {
        this(context, null);
    }

    public XummTxDetailsView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public XummTxDetailsView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public XummTxDetailsView(Context context, AttributeSet attributeSet, int i, int i1) {
        super(context, attributeSet, i, i1);
    }

    public void setData(JSONObject object) {
        showTransactionDetails(object);
    }

    private void showTransactionDetails(JSONObject tx) {
        Map<String, String> map = toMap(tx);
        sortedKeys = new ArrayList<>(map.keySet());
        sortedKeys.sort((o1, o2) -> getDisplayOrder(o1) - getDisplayOrder(o2));
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (String key : sortedKeys) {
            XrpTxItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.xrp_tx_item,null,false);
            binding.title.setText(key+":");
            binding.content.setText(map.get(key));
            addView(binding.getRoot());
        }
    }

    public Map<String, String> toMap(JSONObject jsonObj) {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObj.keys();
        try {
            while(keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObj.get(key);
                map.put(key, value.toString());
            }
            return map;
        } catch (JSONException ignored) {

        }
        return null;
    }

    private int getDisplayOrder(final String key) {
        switch (key) {
            case "TransactionType":
                return 0;
        }
        return Integer.MAX_VALUE;
    }
}
