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
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.cobo.bcUniformResource.UniformResource;
import com.cobo.cold.AppExecutors;
import com.cobo.cold.R;

import java.util.ArrayList;
import java.util.List;

public class DynamicQrCodeView extends LinearLayout implements QrCodeHolder {
    private static final int DURATION = 330; //ms
    private String data;
    private final List<String> splitData;
    public int count;
    private final Cache mCache = Cache.getInstance();
    private ProgressBar progressBar;
    private ImageView img;
    public final Handler handler = new Handler();
    public final Runnable runnable;
    private final AppCompatActivity mActivity;
    private SplitCallback splitCallback;

    enum QrCapacity {
        HIGH(900),
        MID(600),
        LOW(300);

        public int capacity;
        QrCapacity(int i) {
            this.capacity = i;
        }
    }

    public QrCapacity qrCapacity = QrCapacity.HIGH;

    public int currentIndex = 0;

    public boolean autoAnimate = true;

    private boolean multiPart = true;

    public DynamicQrCodeView(Context context) {
        this(context, null);
    }

    public DynamicQrCodeView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mActivity = (AppCompatActivity) context;
        splitData = new ArrayList<>();
        runnable = this::showQrCode;
    }

    public void disableMultipart() {
        multiPart = false;
    }

    public void setSplitCallback(SplitCallback callback) {
        this.splitCallback = callback;
    }

    public void setData(String s) {
        data = s;
        if (multiPart) {
            AppExecutors.getInstance().networkIO().execute(()-> {
                try {
                    String[] workloads = UniformResource.Encoder.encode(data.toUpperCase(), qrCapacity.capacity);
                    count = workloads.length;
                    if (splitCallback != null) {
                        splitCallback.onSplit(count);
                    }
                    splitData.clear();
                    for (int i = 0; i < count; i++) {
                        splitData.add(workloads[i].toUpperCase());
                    }
                    currentIndex = 0;
                    handler.removeCallbacks(runnable);
                    handler.post(runnable);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            count = 1;
            showQrCode();
        }
    }

    public void disableModal() {
        img.setOnClickListener(null);
        findViewById(R.id.hint).setVisibility(View.GONE);
    }

    public void setAutoAnimate(boolean autoAnimate) {
        this.autoAnimate = autoAnimate;
        if (!autoAnimate) {
            handler.removeCallbacks(runnable);
        } else {
            handler.post(runnable);
        }
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
        QrCodeModal modal = QrCodeModal.newInstance(data, multiPart);
        modal.show(mActivity.getSupportFragmentManager(),"");
    }
    public void showQrCode() {
        if (multiPart) {
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
            if (count > 1 && autoAnimate) {
                currentIndex = ++currentIndex % count;
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, DURATION);
            }
        } else {
            if (ViewCompat.isLaidOut(this)) {
                mCache.offer(data, this);
            } else {
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        mCache.offer(data, DynamicQrCodeView.this);
                    }
                });
            }
        }
    }

    private void setImageBitmap(Bitmap bm) {
        AppExecutors.getInstance().mainThread().execute(() -> {
            progressBar.setVisibility(GONE);
            img.setVisibility(VISIBLE);
            img.setImageBitmap(bm);
        });
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