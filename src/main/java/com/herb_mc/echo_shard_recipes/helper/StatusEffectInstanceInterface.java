package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.entity.effect.StatusEffectInstance;

public interface StatusEffectInstanceInterface {

    StatusEffectInstance setAmplifier(int a);
    StatusEffectInstance setDuration(int d);
    StatusEffectInstance setLevelBoost(int d);
    StatusEffectInstance setBoosted(boolean b);
    boolean isBoosted();
    int getLevelBoost();
}
