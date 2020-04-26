/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cobo.cold.ui;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.cobo.cold.ui.views.qrcode.QrCodeView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class BindingAdapters {
    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("android:src")
    public static void setSrc(ImageView view, int resId) {
        view.setImageResource(resId);
    }

    @BindingAdapter("icon")
    public static void setIcon(ImageView view, String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        Context context = view.getContext();
        int resId = context.getResources().getIdentifier("coin_" + name.toLowerCase(),
                "drawable", context.getPackageName());
        view.setImageResource(resId);
    }

    public static void setIcon(ImageView view, @NonNull String coinCode, String name) {
        if (TextUtils.isEmpty(name)) {
            name = coinCode;
        }
        Context context = view.getContext();
        if (!coinCode.equals(name) && !"USDT".equals(name)) {
            name = coinCode + "_token";
        }
        int resId = context.getResources().getIdentifier("coin_" + name.toLowerCase(),
                "drawable", context.getPackageName());
        view.setImageResource(resId);
    }

    @BindingAdapter("data")
    public static void setData(QrCodeView view, String data) {
        if (TextUtils.isEmpty(data)) {
            return;
        }
        view.setData(data);
    }

    @BindingAdapter("underline1")
    public static void setUnderline1(TextView view, boolean underline) {
        if (underline) {
            view.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    @BindingAdapter("time")
    public static void setTimeStamp(TextView view, long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
                Locale.getDefault());
        view.setText(formatter.format(time));
    }
}