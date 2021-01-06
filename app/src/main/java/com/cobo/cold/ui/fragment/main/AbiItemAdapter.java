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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AbiItemAdapter {

    public List<AbiItem> adapt(JSONObject tx) {
        try {
            JSONArray params = tx.getJSONArray("param");
            List<AbiItem> items = new ArrayList<>();
            items.add(new AbiItem("method", tx.getString("method")));
            for (int i = 0; i < params.length(); i++) {
                JSONObject param = params.getJSONObject(i);
                String name = param.getString("name");
                Object value = param.get("value");
                if (value instanceof JSONArray) {
                    JSONArray arr = (JSONArray) value;
                    StringBuilder concatValue = new StringBuilder();
                    for (int j = 0; j < arr.length(); j++) {
                        concatValue.append(arr.getString(j));
                        if (j != arr.length() -1) {
                            concatValue.append(",\n");
                        }
                    }
                    items.add(new AbiItem(name, concatValue.toString()));
                } else {
                    items.add(new AbiItem(name, value.toString()));
                }
            }
            return items;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class AbiItem{
        String key;
        String value;

        public AbiItem(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
