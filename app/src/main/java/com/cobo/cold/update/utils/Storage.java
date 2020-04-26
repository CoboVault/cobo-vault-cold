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

package com.cobo.cold.update.utils;

import android.content.Context;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class Storage {
    private static final String UPDATE_CACHE = "update_cache";
    private static final String UPDATE_ZIP_FILE = "update.zip";

    private final File mInternalDir;
    private final File mExternalDir;

    private Storage(@NonNull File internalDir, @NonNull File externalDir) {
        mInternalDir = internalDir;
        mExternalDir = externalDir;
    }

    public static void resetCacheDir() {
        final File dir = new File(Environment.getExternalStorageDirectory(), UPDATE_CACHE);

        if (dir.exists()) {
            FileUtils.deleteRecursive(dir);
        }

        dir.mkdir();
    }

    @Nullable
    public static Storage createByEnvironment(@NonNull Context context) {
        final File[] result = new File[2];
        final File[] current = context.getExternalFilesDirs(null);

        for (File dir : current) {
            if (dir != null && dir.isDirectory()) {
                if (Environment.isExternalStorageRemovable(dir)) {
                    result[1] = dir;
                } else {
                    result[0] = dir;
                }
            }
        }

        if (result[0] == null || result[1] == null) {
            return null;
        }

        final File internalRootDir = getRootDir(result[0]);
        final File externalRootDir = getRootDir(result[1]);

        return new Storage(internalRootDir, externalRootDir);
    }

    @NonNull
    private static File getRootDir(@NonNull File file) {
        final String currentPath = file.toString();
        final int endIndex = currentPath.indexOf("/Android/data");

        if (endIndex >= 0) {
            return new File(currentPath.substring(0, endIndex));
        } else {
            return file;
        }
    }

    @Nullable
    public File getInternalDir() {
        return mInternalDir;
    }

    @Nullable
    public File getExternalDir() {
        return mExternalDir;
    }

    public void resetUpdateCacheDir() {
        final File dir = new File(mInternalDir, UPDATE_CACHE);

        try {
            if (dir.exists()) {
                FileUtils.deleteRecursive(dir);
            }

            dir.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public File getUpdateCacheDir() {
        final File dir = new File(mInternalDir, UPDATE_CACHE);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    @NonNull
    public File getUpdateZipFile() {
        return new File(mExternalDir, UPDATE_ZIP_FILE);
    }
}