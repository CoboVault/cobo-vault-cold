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

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;

import com.cobo.cold.R;
import com.cobo.cold.ui.common.FullScreenActivity;
import com.cobo.cold.viewmodel.SetupVaultViewModel;

import java.util.Objects;

import static com.cobo.cold.Utilities.IS_SETUP_VAULT;
import static com.cobo.cold.ui.fragment.setup.SetPasswordFragment.PASSWORD;

public class SetupVaultActivity extends FullScreenActivity {

    public boolean isSetupVault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_setup_vault);
        setupNavController(savedInstanceState);

        if (getIntent() != null) {
            String password = getIntent().getStringExtra(PASSWORD);
            SetupVaultViewModel model = ViewModelProviders.of(this).get(SetupVaultViewModel.class);
            model.setPassword(password);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupNavController(Bundle savedInstanceState) {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavInflater inflater = Objects.requireNonNull(navHostFragment).getNavController().getNavInflater();
        NavGraph graph = inflater.inflate(R.navigation.nav_graph_setup);
        Intent intent = getIntent();

        if (savedInstanceState != null) {
            isSetupVault = savedInstanceState.getBoolean(IS_SETUP_VAULT);
        } else {
            isSetupVault = intent != null && intent.getBooleanExtra(IS_SETUP_VAULT, true);
        }
        if (!isSetupVault) {
            graph.setStartDestination(R.id.setupVaultFragment);
        }
        navHostFragment.getNavController().setGraph(graph);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_SETUP_VAULT, isSetupVault);
    }
}
