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

package com.cobo.cold;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cobo.coinlib.v8.ScriptLoader;
import com.cobo.cold.db.AppDatabase;
import com.cobo.cold.encryption.EncryptionCoreProvider;
import com.cobo.cold.logging.FileLogger;
import com.cobo.cold.service.AttackCheckingService;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.UnlockActivity;
import com.cobo.cold.util.HashUtil;

import org.spongycastle.util.encoders.Hex;

import java.lang.ref.SoftReference;

public class MainApplication extends Application {
    private static MainApplication sApplication;
    private AppExecutors mAppExecutors;
    private SoftReference<Activity> topActivity;
    private boolean shouldLock;

    public MainApplication() {
        sApplication = this;
    }

    @NonNull
    public static MainApplication getApplication() {
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAppExecutors = AppExecutors.getInstance();
        EncryptionCoreProvider.getInstance().initialize(this);
        mAppExecutors.diskIO().execute(() -> {
            FileLogger.init(this);
            FileLogger.purgeLogs(this);
        });
        initBackgroundCallBack();
        ScriptLoader.init(this);
        if (TextUtils.isEmpty(Utilities.getRandomSalt(this))) {
            Utilities.setRandomSalt(this, Hex.toHexString(HashUtil.getNextSalt()));
        }

        IntentFilter mScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenOReceiver, mScreenOffFilter);
        shouldLock = Utilities.hasVaultCreated(this);

        startAttackCheckingService();
        resetInputMethodSettings();
    }

    private void resetInputMethodSettings() {
        if (!Utilities.isInputSettingsCleared(this)) {
            new Thread(() -> {
                PackageManager pm = getPackageManager();
                pm.clearApplicationUserData("com.google.android.inputmethod.pinyin", null);
                Utilities.setInputSettingsCleared(this);
            }).start();
        }
    }

    private void startAttackCheckingService() {
        Intent intent = new Intent(this, AttackCheckingService.class);
        startService(intent);
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this, mAppExecutors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(this, getDatabase());
    }

    private void initBackgroundCallBack() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                if (activity instanceof MainActivity && savedInstanceState == null && shouldLock) {
                    Intent intent = new Intent(activity, UnlockActivity.class);
                    activity.startActivity(intent);
                    shouldLock = false;
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                topActivity = new SoftReference<>(activity);
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    private final BroadcastReceiver mScreenOReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Activity activity = topActivity.get();
                if (!(activity instanceof UnlockActivity)
                        && Utilities.hasVaultCreated(activity)
                        && !Utilities.isAttackDetected(activity)) {
                    startActivity(new Intent(activity, UnlockActivity.class));
                }
            }
        }
    };
}
