package com.wondertoys.pokevalue.calculator;

import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

public final class LevelDustCosts {
    //region - Fields -
    private static HashMap<Integer, Integer> stats;
    //region

    //region - Static -
    public static boolean validDustCost(int dustCost) {
        return stats.containsKey(dustCost);
    }

    public static Double[] getLevelRange(int dustCost) {
        if ( validDustCost(dustCost) ) {
            double level = stats.get(dustCost);

            return new Double[] { level, level + 1.5 };
        }

        return null;
    }

    public static Integer[] getDustCosts() {
        ArrayList<Integer> costs = new ArrayList(stats.keySet());

        Collections.sort(costs, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                if ( a == b ) return 0;
                return a > b ? 1 : -1;
            }
        });

        return costs.toArray(new Integer[0]);
    }

    public static void loadData(AssetManager assets) {
        stats = new HashMap<>();

        String json = null;
        try {
            InputStream is = assets.open("dustCosts.json");
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

                int maxLevel = obj.getInt("maxLevel");
                int cost = obj.getInt("cost");

                stats.put(cost, maxLevel);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion
}
