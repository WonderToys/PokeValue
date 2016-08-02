package com.wondertoys.pokevalue.utils;

import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;

public class Pokemon {
    //region - Classes -
    private class PossibleHPIV {
        public LevelUpData levelData;
        public int iv;
    }

    public class PossibleIV {
        public int staminaIV;
        public int attackIV;
        public int defenseIV;

        public int level;

        public double perfection;
    }
    //endregion

    //region - Fields -
    public int id;
    public int stamina;
    public int attack;
    public int defense;

    public String name;

    public ArrayList<PossibleIV> potentialIVs;
    //endregion

    //region - Constructor -
    public Pokemon(int id, String name, int stamina, int attack, int defense) {
        this.id = id;
        this.stamina = stamina;
        this.attack = attack;
        this.defense = defense;

        this.name = name;
    }
    //endregion

    //region - Private Methods -
    private Boolean testHP(int hp, int iv, LevelUpData levelData) {
        return hp == Math.round(Math.floor((this.stamina + iv) * levelData.cpScalar));
    }

    private Boolean testCP(int cp, int attackIV, int defenseIV, int staminaIV, LevelUpData levelData) {
        double attackFactor = this.attack + attackIV;
        double defenseFactor = Math.pow(this.defense + defenseIV, 0.5);
        double staminaFactor = Math.pow(this.stamina + staminaIV, 0.5);
        double scalarFactor = Math.pow(levelData.cpScalar, 2);

        return cp == (int)Math.round((attackFactor * defenseFactor * staminaFactor * scalarFactor) / 10);
    }

    private ArrayList<PossibleHPIV> getPossibleHPIVs(EvaluationData evaluationData, ArrayList<LevelUpData> potentialLevels) {
        ArrayList<PossibleHPIV> list = new ArrayList<>();

        for ( LevelUpData levelData : potentialLevels ) {
            for ( int stamIndex = 0; stamIndex <= 15; stamIndex++ ) {
                if ( testHP(evaluationData.hitPoints, stamIndex, levelData) ) {
                    PossibleHPIV phpiv = new PossibleHPIV();
                    phpiv.levelData = levelData;
                    phpiv.iv = stamIndex;

                    list.add(phpiv);
                }
            }
        }

        return list;
    }

    private ArrayList<PossibleIV> getPossibleIVs(EvaluationData evaluationData, ArrayList<PossibleHPIV> possibleHPIVs) {
        ArrayList<PossibleIV> list = new ArrayList<>();

        for ( PossibleHPIV phpiv : possibleHPIVs ) {
            for ( int attackIV = 0; attackIV <= 15; attackIV++ ) {
                for ( int defenseIV = 0; defenseIV <= 15; defenseIV++ ) {
                    if ( testCP(evaluationData.combatPower, attackIV, defenseIV, phpiv.iv, phpiv.levelData) ) {
                        PossibleIV piv = new PossibleIV();
                        piv.staminaIV = phpiv.iv;
                        piv.attackIV = attackIV;
                        piv.defenseIV = defenseIV;
                        piv.level = phpiv.levelData.level;

                        double perfection = ((double)phpiv.iv + (double)attackIV + (double)defenseIV) / 45.0;
                        piv.perfection = (Math.floor(perfection * 100) / 100) * 100;

                        list.add(piv);
                    }
                }
            }
        }

        return list;
    }
    //endregion

    //region - Public Methods -
    public void evaluate(EvaluationData evaluationData, ArrayList<LevelUpData> levelUpData) {
        this.potentialIVs = new ArrayList<>();

        // Get possible levels by dust
        ArrayList<LevelUpData> possibleLevels = new ArrayList<>();
        for ( LevelUpData data : levelUpData ) {
            if (data.dust == evaluationData.dustCost) {
                if (evaluationData.poweredUp == true || (data.level % 2) != 0) {
                    possibleLevels.add(data);
                }
            }
        }

        ArrayList<PossibleIV> possibleIVs = getPossibleIVs(evaluationData, getPossibleHPIVs(evaluationData, possibleLevels));
        Collections.sort(possibleIVs, new Comparator<PossibleIV>() {
            public int compare(PossibleIV a, PossibleIV b) {
                if ( a.perfection == b.perfection ) return 0;

                return a.perfection > b.perfection ? 1 : -1;
            }
        });

        this.potentialIVs = possibleIVs;
    }
    //endregion

    //region - Static -
    public static ArrayList<Pokemon> loadAllPokemon(AssetManager assets) {
        ArrayList<Pokemon> list = new ArrayList<Pokemon>();

        String json = null;
        try {
            InputStream is = assets.open("pokemon.json");
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

                int id = obj.getInt("id");
                int stamina = obj.getInt("stamina");
                int attack = obj.getInt("attack");
                int defense = obj.getInt("defense");
                String name = obj.getString("name");

                list.add(new Pokemon(id, name, stamina, attack, defense));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
    //endregion
}
