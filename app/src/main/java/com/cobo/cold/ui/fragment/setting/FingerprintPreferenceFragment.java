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

package com.cobo.cold.ui.fragment.setting;

import android.app.Activity;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cobo.cold.R;
import com.cobo.cold.Utilities;
import com.cobo.cold.ui.preference.SwitchPreference;

import static com.cobo.cold.Utilities.SHARED_PREFERENCES_KEY;

public class FingerprintPreferenceFragment extends PreferenceFragmentCompat {

    public static final String FINGERPRINT_UNLOCK = "fingerprint_unlock";
    public static final String FINGERPRINT_SIGN = "fingerprint_sign";

    private SwitchPreference fingerprintUnlock;
    private SwitchPreference fingerprintSign;

    private Activity mActivity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_KEY);
        addPreferencesFromResource(R.xml.fingerprint_preference);
    }

    @Override
    public void onResume() {
        super.onResume();
        fingerprintUnlock = findPreference(FINGERPRINT_UNLOCK);
        if (fingerprintUnlock != null) {
            fingerprintUnlock.setChecked(Utilities.isFingerprintUnlockEnable(mActivity));
        }

        fingerprintSign = findPreference(FINGERPRINT_SIGN);
        if (fingerprintSign != null) {
            fingerprintSign.setChecked(Utilities.isFingerprintSignEnable(mActivity));
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case FINGERPRINT_UNLOCK:
                fingerprintUnlock.setChecked(!fingerprintUnlock.isChecked());
                Utilities.setFingerprintUnlockEnable(mActivity, fingerprintUnlock.isChecked());
                break;
            case FINGERPRINT_SIGN:
                fingerprintSign.setChecked(!fingerprintSign.isChecked());
                Utilities.setFingerprintSignEnable(mActivity, fingerprintSign.isChecked());
                break;
        }

        return super.onPreferenceTreeClick(preference);
    }

}
