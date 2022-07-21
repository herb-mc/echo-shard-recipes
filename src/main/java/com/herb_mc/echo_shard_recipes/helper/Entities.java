package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;

public class Entities {

    public static boolean isInorganic(LivingEntity e) {
        return e instanceof ShulkerEntity || e instanceof IronGolemEntity || e instanceof SkeletonEntity || e instanceof WitherSkeletonEntity || e instanceof WitherEntity || e instanceof BlazeEntity || e instanceof SkeletonHorseEntity || e instanceof StrayEntity || e instanceof EndermanEntity;
    }

    public static void damageEntity(Entity e, float damage, float dragonDamage, boolean ignoreIframes, DamageSource damageSource) {
        if (e instanceof EnderDragonPart) {
            ((EnderDragonPart) e).owner.hurtTime = 0;
            ((EnderDragonPart) e).owner.timeUntilRegen = 1;
            ((EnderDragonPart) e).owner.damagePart((EnderDragonPart) e, damageSource, dragonDamage);
        } else {
            if (e instanceof LivingEntity) ((LivingEntity) e).hurtTime = 0;
            e.timeUntilRegen = 1;
            e.damage(damageSource, damage);
        }
    }

}
