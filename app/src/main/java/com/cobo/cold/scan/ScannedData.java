/*
 * Copyright (c) 2020 Cobo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cobo.cold.scan;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class ScannedData {
    public final int index;
    public final int total;
    public final String checkSum;
    public final String value;
    public final boolean compress;
    public final String rawString;
    public final String valueType;


    public ScannedData(int index, int total, String checkSum, String value, boolean compress,
                       String rawString, String valueType) {
        this.index = index;
        this.total = total;
        this.checkSum = checkSum;
        this.value = value;
        this.compress = compress;
        this.rawString = rawString;
        this.valueType = valueType;
    }

    static ScannedData fromJson(JSONObject jsonObject) throws JSONException {
        int index = jsonObject.getInt("index");
        int total = jsonObject.getInt("total");
        String checkSum = jsonObject.getString("checkSum");
        String value = jsonObject.getString("value");
        String valueType = jsonObject.optString("valueType");
        boolean compress = jsonObject.getBoolean("compress");
        if (index < 0 || total < 1 || index >= total || TextUtils.isEmpty(checkSum)) {
            throw new JSONException("");
        }
        return new ScannedData(index, total, checkSum, value, compress, jsonObject.toString(), valueType);
    }

    @NonNull
    @Override
    public String toString() {
        return "ScannedData{" +
                "index=" + index +
                ", total=" + total +
                ", checkSum='" + checkSum + '\'' +
                ", value='" + value + '\'' +
                ", compress=" + compress +
                ", rawString='" + rawString + '\'' +
                ", valueType='" + valueType + '\'' +
                '}';
    }


}
