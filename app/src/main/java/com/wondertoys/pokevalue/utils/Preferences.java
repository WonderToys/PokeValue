package com.wondertoys.pokevalue.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.preference.PreferenceManager;

public class Preferences {
    //region - Constants -
    private static final String KEY_TOGGLE_OVERLAY_X = "toggleOverlayX";
    private static final String KEY_TOGGLE_OVERLAY_Y = "toggleOverlayY";

    private static final String KEY_CALCULATE_OVERLAY_X = "calculateOverlayX";
    private static final String KEY_CALCULATE_OVERLAY_Y = "calculateOverlayY";

    private static final int DEFAULT_TOGGLE_OVERLAY_X = 0;
    private static final int DEFAULT_TOGGLE_OVERLAY_Y = 250;

    private static final int DEFAULT_CALCULATE_OVERLAY_X = -1;
    private static final int DEFAULT_CALCULATE_OVERLAY_Y = 250;
    //endregioin

    //region - Static  -
    public static Point getToggleOverlayLocation(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        int x = sharedPreferences.getInt(KEY_TOGGLE_OVERLAY_X, DEFAULT_TOGGLE_OVERLAY_X);
        int y = sharedPreferences.getInt(KEY_TOGGLE_OVERLAY_Y, DEFAULT_TOGGLE_OVERLAY_Y);

        return new Point(x, y);
    }

    public static boolean setToggleOverlayLocation(Context context, Point point) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_TOGGLE_OVERLAY_X, point.x);
        editor.putInt(KEY_TOGGLE_OVERLAY_Y, point.y);

        return editor.commit();
    }

    public static Point getCalculateOverlayLocation(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        int x = sharedPreferences.getInt(KEY_CALCULATE_OVERLAY_X, DEFAULT_CALCULATE_OVERLAY_X);
        int y = sharedPreferences.getInt(KEY_CALCULATE_OVERLAY_Y, DEFAULT_CALCULATE_OVERLAY_Y);

        return new Point(x, y);
    }
    //endregion
}
