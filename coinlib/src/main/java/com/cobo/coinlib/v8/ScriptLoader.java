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

package com.cobo.coinlib.v8;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.eclipsesource.v8.V8;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScriptLoader {

    @SuppressLint("StaticFieldLeak")
    public static ScriptLoader sInstance;
    private final Context context;

    private ScriptLoader(Context context) {
        this.context = context;
    }

    public static void init(Context context) {
        if (sInstance == null) {
            synchronized (ScriptLoader.class) {
                if (sInstance == null) {
                    sInstance = new ScriptLoader(context);
                }
            }
        }
    }

    public V8 loadByCoinCode(String coinCode) {
        V8 v8 = V8.createV8Runtime("window");
        String js = getJs(context, coinCode);
        if (!TextUtils.isEmpty(js) && !v8.isReleased()) {
            v8.executeVoidScript(js);
        }
        return v8;
    }

    public V8 loadByFileName(String fileName) {
        V8 v8 = V8.createV8Runtime("window");
        String js = readAsset(context.getAssets(),fileName);
        if (!TextUtils.isEmpty(js) && !v8.isReleased()) {
            v8.executeVoidScript(js);
        }
        return v8;
    }

    private String getJs(Context context, String coinCode) {
        AssetManager am = context.getAssets();
        try {
            JSONObject bundleMap = new JSONObject(readAsset(am, "bundleMap.json"));
            return readAsset(am, "script/" + bundleMap.getString(coinCode));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readAsset(AssetManager am, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = am.open(fileName);
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line).append("\r\n");
            }
            bf.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}
