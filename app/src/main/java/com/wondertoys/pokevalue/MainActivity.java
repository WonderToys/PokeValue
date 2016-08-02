package com.wondertoys.pokevalue;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;

public class MainActivity extends Activity {
    public final static int REQUEST_CODE = 1234;

    private void showToggleOverlay() {
        Intent intent = new Intent(this, ToggleOverlayService.class);
        startService(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
}