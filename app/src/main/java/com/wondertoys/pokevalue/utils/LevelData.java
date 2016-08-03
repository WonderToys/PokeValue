package com.wondertoys.pokevalue.utils;

import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public final class LevelData {
    //region - Fields -
    private static HashMap<Double, Double> stats;
    //region

    //region - Static -
    public static double getCpScalar(double level) {
        return stats.get(level);
    }

    public static void loadData(AssetManager assets) {
        stats = new HashMap<>();

        String json = null;
        try {
            InputStream is = assets.open("levelData.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }

        try {
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                double level = obj.getDouble("level");
                double cpScalar = obj.getDouble("cpScalar");

                stats.put(level, cpScalar);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion
}
