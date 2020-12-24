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

package com.cobo.coinlib;

import android.content.Context;

import com.cobo.coinlib.v8.ScriptLoader;

public class Coinlib {
    public static Coinlib sInstance;
    private final Context context;

    private Coinlib(Context context) {
        this.context = context;
        ScriptLoader.init(context);
    }

    public Context getContext(){
        return context;
    }

    public static void init(Context context) {
        if (sInstance == null) {
            synchronized (ScriptLoader.class) {
                if (sInstance == null) {
                    sInstance = new Coinlib(context);
                }
            }
        }
    }
}