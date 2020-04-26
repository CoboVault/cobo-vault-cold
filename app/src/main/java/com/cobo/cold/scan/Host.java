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

import android.os.Handler;

import com.cobo.cold.scan.bean.ZxingConfig;
import com.cobo.cold.scan.camera.CameraManager;
import com.cobo.cold.scan.view.PreviewFrame;

public interface Host {
    ZxingConfig getConfig();

    PreviewFrame getFrameView();

    void handleDecode(String res);

    void handleDecode(ScannedData[] res);

    void handleProgress(int total, int scan);

    CameraManager getCameraManager();

    Handler getHandler();
}
