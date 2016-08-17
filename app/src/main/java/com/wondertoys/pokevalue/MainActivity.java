package com.wondertoys.pokevalue;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.wondertoys.pokevalue.utils.AutoUpdateApk;
import com.wondertoys.pokevalue.utils.Preferences;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final static int REQUEST_CODE = 1234;

    private AutoUpdateApk autoUpdateApk;

    private void showToggleOverlay() {
        Intent intent = new Intent(this, ToggleOverlayService.class);
        startService(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_general, false);

        if ( Preferences.getEnableAutoUpdate(this) ) {
            autoUpdateApk = new AutoUpdateApk(getApplicationContext());
        }

        if (Build.VERSION.SDK_INT >= 23 ) {
            if ( !Settings.canDrawOverlays(this) ) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
            else {
                showToggleOverlay();
            }
        }
        else {
            showToggleOverlay();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (Build.VERSION.SDK_INT >= 23 ) {
            if (requestCode == REQUEST_CODE) {
                if ( Settings.canDrawOverlays(this) ) {
                    showToggleOverlay();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (Build.VERSION.SDK_INT >= 23 ) {
            if ( !Settings.canDrawOverlays(this) ) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
            else {
                showToggleOverlay();
            }
        }
        else {
            showToggleOverlay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        autoUpdateApk = null;
    }
}