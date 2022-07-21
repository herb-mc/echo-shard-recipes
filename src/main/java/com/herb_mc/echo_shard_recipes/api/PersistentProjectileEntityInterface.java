package com.herb_mc.echo_shard_recipes.api;

public interface PersistentProjectileEntityInterface {

    int getParticle();
    String getAttribute();
    void setParticle(int i);
    void setAttribute(String s);;
    void addFlatDamage(int i);
    void addDamageMultiplier(float f);

}
