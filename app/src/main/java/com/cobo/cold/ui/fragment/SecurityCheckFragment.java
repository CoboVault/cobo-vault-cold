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

package com.cobo.cold.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.selfcheck.SecurityCheck;
import com.cobo.cold.ui.AttackWarningActivity;
import com.cobo.cold.ui.MainActivity;
import com.cobo.cold.ui.SetupVaultActivity;

import java.util.concurrent.Executors;

import static com.cobo.cold.selfcheck.SecurityCheck.RESULT_OK;

public class SecurityCheckFragment extends BaseFragment {

    @Override
    protected int setView() {
        return R.layout.security_check;
    }

    @Override
    protected void init(View view) {
        Handler handler = new Handler();
        Executors.newSingleThreadExecutor().execute(() -> {
            SecurityCheck.CheckResult checkResult = new SecurityCheck().doSelfCheck(mActivity);
            handler.postDelayed(() -> {
                if (checkResult.result == RESULT_OK) {
                    boolean vaultCreated = Utilities.hasVaultCreated(mActivity);
                    Log.d(TAG, "vaultCreated = " + vaultCreated);
                    Intent intent;
                    if (vaultCreated) {
                        intent = new Intent(mActivity, MainActivity.class);
                    } else {
                        intent = new Intent(mActivity, SetupVaultActivity.class);
                        intent.putExtra("check_updating", true);
                    }
                    startActivity(intent);
                    mActivity.finish();
                } else {
                    Utilities.setAttackDetected(mActivity);
                    Bundle data = new Bundle();
                    data.putInt("firmware", checkResult.firmwareStatusCode);
                    data.putInt("system", checkResult.systemStatusCode);
                    data.putInt("signature", checkResult.signatureStatusCode);
                    Intent intent = new Intent(mActivity, AttackWarningActivity.class);
                    intent.putExtras(data);
                    startActivity(intent);
                }
            }, 1500);
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }
}
