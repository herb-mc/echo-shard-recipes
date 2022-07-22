package com.herb_mc.echo_shard_recipes.api;

public interface ManaPlayer {

    double getMana();
    boolean tryRemoveMana(double d);
    void addMana(double d);
    void setMana(double d);
    void addManaRegenBoost(double d);
    void setManaRegenBoost(double d);
    double getManaRegenBoost(double d);
    double getManaRegen();
    double getMaxMana();

}
