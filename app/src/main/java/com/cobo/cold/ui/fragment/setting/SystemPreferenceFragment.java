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

import android.os.Bundle;

import androidx.navigation.Navigation;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cobo.cold.R;
import com.cobo.cold.setting.VibratorHelper;
import com.cobo.cold.ui.preference.SwitchPreference;

import java.util.Objects;

import static com.cobo.cold.Utilities.SHARED_PREFERENCES_KEY;
import static com.cobo.cold.ui.fragment.Constants.KEY_TITLE;

public class SystemPreferenceFragment extends PreferenceFragmentCompat {

    public static final String SETTING_LANGUAGE = "setting_language";
    public static final String SETTING_VIBRATOR = "setting_vibrator";
    private static final String SETTING_BRIGHTNESS = "setting_brightness";
    static final String SETTING_SCREEN_OFF_TIME = "setting_screen_off_time";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_KEY);
        addPreferencesFromResource(R.xml.system_preference);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Bundle data = new Bundle();
        switch (preference.getKey()) {
            case SETTING_LANGUAGE:
                data.putInt(KEY_TITLE, R.string.setting_language);
                Navigation.findNavController(Objects.requireNonNull(getView()))
                        .navigate(R.id.action_to_languagePreferenceFragment, data);
                break;
            case SETTING_VIBRATOR:
                SwitchPreference switchPreference = (SwitchPreference) preference;
                final boolean newValue = !switchPreference.isChecked();
                if (switchPreference.callChangeListener(newValue)) {
                    switchPreference.setChecked(newValue);
                }
                if (newValue) {
                    VibratorHelper.vibrate(Objects.requireNonNull(getActivity()));
                }
                break;
            case SETTING_BRIGHTNESS:
                Navigation.findNavController(Objects.requireNonNull(getView()))
                        .navigate(R.id.action_to_brightnessSettingFragment);
                break;
            case SETTING_SCREEN_OFF_TIME:
                data.putInt(KEY_TITLE, R.string.setting_screen_off_time);
                Navigation.findNavController(Objects.requireNonNull(getView()))
                        .navigate(R.id.action_to_screenOffPreferenceFragment, data);
                break;

        }
        return super.onPreferenceTreeClick(preference);
    }

}
