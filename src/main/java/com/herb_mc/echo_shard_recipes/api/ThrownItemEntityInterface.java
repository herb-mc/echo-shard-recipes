package com.herb_mc.echo_shard_recipes.api;

public interface ThrownItemEntityInterface {

    String getAttribute();
    void setAttribute(String s);
    void addBonusDamage(float f);
    void setIncrement(float f);
    float getIncrement();
    void setBonusDamage(float f);
    float getBonusDamage();
    void setDamage(float f);
    float getDamage();

}
