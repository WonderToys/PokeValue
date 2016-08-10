package com.wondertoys.pokevalue;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.wondertoys.pokevalue.utils.AutoUpdateApk;

import java.util.List;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getFragmentManager().beginTransaction().replace(R.id.settings_content_frame, new GeneralPreferenceFragment()).commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        startService(new Intent(this, ToggleOverlayService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();

        startService(new Intent(this, ToggleOverlayService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

        stopService(new Intent(this, ToggleOverlayService.class));
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            setHasOptionsMenu(false);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if ( preference.getKey().equals("checkUpdate") ) {
                AutoUpdateApk aua = new AutoUpdateApk(getActivity().getApplicationContext());
                aua.checkUpdatesManually();

                return true;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
