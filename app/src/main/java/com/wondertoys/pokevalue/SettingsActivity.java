package com.wondertoys.pokevalue;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.wondertoys.pokevalue.utils.AutoUpdateApk;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
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

            PackageInfo info;
            try {
                info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                findPreference("appVersion").setSummary(String.format("v%s", info.versionName));
            } catch (PackageManager.NameNotFoundException e) {
                findPreference("appVersion").setSummary("ERROR");
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if ( preference.getKey().equals("checkUpdate") ) {
                AutoUpdateApk aua = new AutoUpdateApk(getActivity().getApplicationContext());
                aua.checkUpdatesManually();

                return true;
            }
            else if ( preference.getKey().equals("releaseNotes") ) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/WonderToys/PokeValue/blob/master/ReleaseNotes.md"));
                startActivity(intent);

                return true;
            }
            else if ( preference.getKey().equals("reportBug") ) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/WonderToys/PokeValue/issues"));
                startActivity(intent);

                return true;
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
