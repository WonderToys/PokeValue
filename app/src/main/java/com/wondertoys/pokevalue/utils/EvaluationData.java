package com.wondertoys.pokevalue.utils;

public class EvaluationData {
    //region - Fields -
    public Pokemon pokemon;

    public int combatPower;
    public int hitPoints;
    public int dustCost;

    public Boolean poweredUp;
    //endregion

    //region - Constructor -
    public EvaluationData(Pokemon pokemon, int cp, int hp, int dust, Boolean poweredUp) {
        this.pokemon = pokemon;

        this.combatPower = cp;
        this.hitPoints = hp;
        this.dustCost = dust;

        this.poweredUp = poweredUp;
    }
    //endregion
}
