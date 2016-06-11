package com.example.mohammed.myapplication;


import android.annotation.TargetApi;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

/**
 * Created by mohammed on 6/1/16.
 */
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.city_name)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.ringtone_pref)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.vibrate_pref)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.athan_sound)));


    }


    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.

        if (preference instanceof CheckBoxPreference) {
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getBoolean(preference.getKey(), false));

        } else {
            onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));

        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        Intent intent = null;
        try {
            intent = super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        } catch (Exception e) {

        }
        return intent;
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object obj) {
        String stringValue = obj.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (preference instanceof RingtonePreference) {
            Uri ringtoneUri = Uri.parse(stringValue);
            Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            String name = ringtone.getTitle(this);

            // RingtonePreference ringtone=(RingtonePreference) preference;
            preference.setSummary(name);


        } else if (preference instanceof CheckBoxPreference) {
            String on_off = "";
            CheckBoxPreference boxPreference = (CheckBoxPreference) preference;
            boxPreference.setChecked(Boolean.parseBoolean(stringValue));
            if (boxPreference.isChecked()) {
                on_off = "On";
            } else {
                on_off = "Off";
            }
            preference.setSummary(on_off);

        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
        return true;
    }
}
