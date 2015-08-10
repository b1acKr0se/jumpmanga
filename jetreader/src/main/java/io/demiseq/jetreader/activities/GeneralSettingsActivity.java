package io.demiseq.jetreader.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import io.demiseq.jetreader.R;

public class GeneralSettingsActivity extends AppCompatActivity {
    public static final String KEY_DOWNLOAD_NUM = "pref_num_of_downloads";
    public static final String KEY_SHOW_NOTIFICATION = "pref_show_notification";
    public static final String KEY_UPDATE_FREQUENCY = "pref_update_frequency";
    public static final String KEY_AUTO_PRELOAD = "pref_preload";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.general_nested_preference);
            final Preference downloadNumPref = getPreferenceManager().findPreference(KEY_DOWNLOAD_NUM);
            downloadNumPref.setSummary(((ListPreference) downloadNumPref).getEntry());
            downloadNumPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (downloadNumPref instanceof ListPreference)
                        downloadNumPref.setSummary(((ListPreference) downloadNumPref).getEntry());
                    return true;
                }
            });

            final Preference updatePref = getPreferenceManager().findPreference(KEY_UPDATE_FREQUENCY);
            updatePref.setSummary(((ListPreference) updatePref).getEntry());
            updatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (updatePref instanceof ListPreference)
                        updatePref.setSummary(((ListPreference) updatePref).getEntry());
                    return true;
                }
            });

        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case KEY_DOWNLOAD_NUM:
                    ListPreference downloadNumPref = (ListPreference) findPreference(key);
                    downloadNumPref.setSummary(downloadNumPref.getEntry());
                    System.out.println(downloadNumPref.getEntry());
                    break;
                case KEY_UPDATE_FREQUENCY:
                    ListPreference updatePref = (ListPreference) findPreference(key);
                    updatePref.setSummary(updatePref.getEntry());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

    }
}
