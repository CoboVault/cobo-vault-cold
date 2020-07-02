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

package com.cobo.cold.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.content.Context.MODE_PRIVATE;
import static com.cobo.cold.Utilities.IS_MAINNET_MDOE;
import static com.cobo.cold.Utilities.PREFERENCE_SECRET;

public class StatusBarView extends LinearLayout {

    private static final String TAG = "Vault.StatusBarView";

    private final SharedPreferences sp;

    private final OnSharedPreferenceChangeListener listener = (sp, key) -> {
        if (IS_MAINNET_MDOE.equals(key)) {
            updateBg();
        }
    };

    public StatusBarView(Context context) {
        this(context, null);
    }

    public StatusBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public StatusBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        sp = getContext().getSharedPreferences(PREFERENCE_SECRET, MODE_PRIVATE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateBg();
    }

    private void updateBg() {
        boolean isMainNet = Utilities.isMainNet(getContext());
        if (!isMainNet) {
            setBackgroundColor(Color.parseColor("#FF0000"));
            findViewById(R.id.text).setVisibility(VISIBLE);
            ((TextView)findViewById(R.id.text)).setText(R.string.testnet);
        } else {
            setBackgroundColor(Color.TRANSPARENT);
            findViewById(R.id.text).setVisibility(GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }

    private boolean isDryCell() {
        boolean isDryCell = false;
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        try {
            Method method = PowerManager.class.getMethod("isDryCell");
            isDryCell = (boolean) method.invoke(pm);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "[isDryCell] isDryCell = " + isDryCell);
        return isDryCell;
    }
}
