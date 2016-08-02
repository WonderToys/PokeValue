package com.wondertoys.pokevalue.utils;

import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LevelUpData {
    //region - Fields -
    public int level;
    public int dust;
    public int candy;

    public double cpScalar;
    //endregion

    //region - Constructor -
    public LevelUpData(int level, int dust, int candy, double cpScalar) {
        this.level = level;
        this.dust = dust;
        this.candy = candy;
        this.cpScalar = cpScalar;
    }
    //endregion

    //region - Static -
    public static ArrayList<LevelUpData> loadAllLevelUpData(AssetManager assets) {
        ArrayList<LevelUpData> list = new ArrayList<LevelUpData>();

        String json = null;
        try {
            InputStream is = assets.open("levelUpData.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
            return list;
        }

        try {
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);

                int level = obj.getInt("level");
                int dust = obj.getInt("dust");
                int candy = obj.getInt("candy");
                double cpScalar = obj.getDouble("cpScalar");

                list.add(new LevelUpData(level, dust, candy, cpScalar));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
    //endregion
}
