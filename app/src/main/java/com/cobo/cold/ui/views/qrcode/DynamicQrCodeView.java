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

package com.cobo.cold.ui.views.qrcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;

import com.cobo.cold.AppExecutors;
import com.cobo.cold.R;
import com.cobo.cold.databinding.DynamicQrcodeModalBinding;
import com.cobo.cold.encryptioncore.utils.ByteFormatter;
import com.cobo.cold.ui.modal.FullScreenModal;
import com.cobo.cold.update.utils.Digest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DynamicQrCodeView extends LinearLayout implements QrCodeHolder {

    private static final int CAPACITY = 800;
    private static final int DURATION = 330; //ms
    private String data;
    private final List<String> splitData;
    private String checksum;
    private int count;
    private final Cache mCache = Cache.getInstance();
    private ProgressBar progressBar;
    private ImageView img;

    int currentIndex = 0;

    public DynamicQrCodeView(Context context) {
        this(context, null);
    }

    public DynamicQrCodeView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        splitData = new ArrayList<>();
    }

    public void setData(String s) {
        data = s;
        checksum = checksum(data);
        count = (int) Math.ceil(data.length() / (float) CAPACITY);
        splitData();
        showQrCode();
    }

    public void disableModal() {
        img.setOnClickListener(null);
        findViewById(R.id.hint).setVisibility(View.GONE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        img = findViewById(R.id.img);
        progressBar = findViewById(R.id.progress);
        img.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(data)) {
                showModal();
            }
        });
        findViewById(R.id.hint).setOnClickListener(v -> {
            if (!TextUtils.isEmpty(data)) {
                showModal();
            }
        });

    }


    private void showModal() {
        FullScreenModal dialog = new FullScreenModal();
        DynamicQrcodeModalBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.dynamic_qrcode_modal, null, false);
        dialog.setBinding(binding);
        binding.close.setOnClickListener(v -> dialog.dismiss());
        binding.qrcodeLayout.qrcode.setData(data);
        binding.qrcodeLayout.qrcode.disableModal();
        dialog.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "");
    }

    private void showQrCode() {
        if (ViewCompat.isLaidOut(this)) {
            mCache.offer(splitData.get(currentIndex), this);
        } else {
            getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mCache.offer(splitData.get(currentIndex), DynamicQrCodeView.this);
                }
            });
        }
        if (count > 1) {
            currentIndex = ++currentIndex % count;
            postDelayed(this::showQrCode, DURATION);
        }
    }

    private void setImageBitmap(Bitmap bm) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            progressBar.setVisibility(GONE);
            img.setVisibility(VISIBLE);
            img.setImageBitmap(bm);
        });
    }

    void splitData() {
        int partLength = (int) Math.ceil(data.length() / (float) count);
        splitData.clear();
        for (int i = 0; i < count; i++) {
            String part = data.substring(partLength * i,
                    Math.min(partLength * (i + 1), data.length()));
            formatPartData(i, part);
        }
    }

    void formatPartData(int index, String data) {
        JSONObject object = new JSONObject();
        try {
            object.put("total", count);
            object.put("index", index);
            object.put("checkSum", checksum);
            object.put("value", data);
            object.put("compress", true);
            object.put("valueType", "protobuf");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        splitData.add(object.toString());
    }

    private String checksum(String msg) {
        return ByteFormatter.bytes2hex(Digest.MD5.checksum(msg));
    }

    @Override
    public void onRender(Bitmap bm) {
        setImageBitmap(bm);
    }

    @Override
    public int getViewWidth() {
        return img.getWidth();
    }

    @Override
    public int getViewHeight() {
        return img.getHeight();
    }
}
