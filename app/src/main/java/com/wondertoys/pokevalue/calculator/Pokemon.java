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

public class Pokemon {
    //region - Classes -
    public static class Potential {
        public int attackIV;
        public int defenseIV;
        public int staminaIV;

        public int attack;
        public int defense;
        public int stamina;

        public int cp;

        public double level;
        public double perfection;
    }
    //endregion

    //region - Fields -
    public int stamina;
    public int attack;
    public int defense;

    public String name;
    //endregion

    //region - Constructor -
    public Pokemon(String name, int attack, int defense, int stamina) {
        this.attack = attack;
        this.defense = defense;
        this.stamina = stamina;

        this.name = name;
    }
    //endregion

    //region - Private Methods -
    private double getDerivedCP(int attackIV, int defenseIV, int staminaIV, double cpScalar) {
        double derivedAttack = attack + attackIV;
        double derivedDefense = Math.pow(defense + defenseIV, 0.5);
        double derivedStamina = Math.pow(stamina + staminaIV, 0.5);
        double derivedScalar = Math.pow(cpScalar, 2.0);

        return Math.floor((derivedAttack * derivedDefense * derivedStamina * derivedScalar) / 10.0);
    }

    private boolean hpCheck(int hp, int staminaIV, double cpScalar) {
        double derived = Math.floor((this.stamina + staminaIV) * cpScalar);
        return Math.max(10, derived) == hp;
    }

    private boolean cpCheck(int cp, int attackIV, int defenseIV, int staminaIV, double cpScalar) {
        double derivedCP = getDerivedCP(attackIV, defenseIV, staminaIV, cpScalar);
        return Math.max(10, derivedCP) == cp;
    }
    //endregion

    //region - Public Methods -
    public Potential getPotential(int attackIV, int defenseIV, int staminaIV, double cpScalar) {
        double derivedAttack = attack + attackIV;
        double derivedDefense = Math.pow(defense + defenseIV, 0.5);
        double derivedStamina = Math.pow(stamina + staminaIV, 0.5);
        double derivedScalar = Math.pow(cpScalar, 2.0);
        double derivedCP = Math.floor((derivedAttack * derivedDefense * derivedStamina * derivedScalar) / 10.0);

        double perfection = (attackIV + defenseIV + staminaIV) / 45.0;

        Potential potential = new Potential();
        potential.attackIV = attackIV;
        potential.defenseIV = defenseIV;
        potential.staminaIV = staminaIV;

        potential.attack = (int)derivedAttack;
        potential.defense = defense + defenseIV;
        potential.stamina = stamina + staminaIV;

        potential.perfection = Math.round(perfection * 100);
        potential.cp = (int)derivedCP;

        return potential;
    }

    public ArrayList<Potential> evaluate(int cp, int hp, int dust, boolean powered) {
        ArrayList<Potential> list = new ArrayList<>();

        Double[] levels = LevelDustCosts.getLevelRange(dust);
        double minLevel = levels[0];
        double maxLevel = levels[1];

        // EMBRACE THE BRUTE
        for ( int attackIV = 0; attackIV <= 15; attackIV++ ) {
            for ( int defenseIV = 0; defenseIV <= 15; defenseIV++ ) {
                for ( int staminaIV = 0; staminaIV <= 15; staminaIV++ ) {
                    for ( double level = minLevel; level <= (maxLevel + 1); level += 0.50 ) {
                        if ( powered == false && (level % 1) != 0 ) continue;

                        double cpScalar = LevelData.getCpScalar(level);

                        boolean hpOk = hpCheck(hp, staminaIV, cpScalar);
                        boolean cpOk = cpCheck(cp, attackIV, defenseIV, staminaIV, cpScalar);

                        if ( hpOk == true && cpOk == true ) {
                            Potential pot = getPotential(attackIV, defenseIV, staminaIV, cpScalar);
                            pot.level = level;

                            list.add(pot);
                        }
                    }
                }
            }
        }

        Collections.sort(list, new Comparator<Potential>() {
            @Override
            public int compare(Potential a, Potential b) {
                if ( a.perfection == b.perfection ) return 0;
                return a.perfection > b.perfection ? 1 : -1;
            }
        });

        return list;
    }
    //endregion

    //region - Static -
    public static ArrayList<Pokemon> loadAllPokemon(AssetManager assets) {
        ArrayList<Pokemon> list = new ArrayList<>();

        String json;
        try {
            InputStream is = assets.open("baseStats.json");
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

                String name = obj.getString("name");
                int attack = obj.getInt("baseAttack");
                int defense = obj.getInt("baseDefense");
                int stamina = obj.getInt("baseStamina");

                list.add(new Pokemon(name, attack, defense, stamina));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }
    //endregion
}
