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

package com.cobo.cold.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cobo.cold.R;
import com.cobo.cold.ui.common.FullScreenActivity;
import com.cobo.cold.ui.fragment.AttackWarningFragment;

public class AttackWarningActivity extends FullScreenActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attack_warning);

        Bundle data = getIntent().getExtras();
        AttackWarningFragment fragment =  AttackWarningFragment.newInstance(data);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container,fragment)
                .commit();


    }
}
