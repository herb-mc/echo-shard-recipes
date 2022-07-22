package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;

import java.util.UUID;

import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.addAttribute;
import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.removeAttribute;
import static net.minecraft.entity.attribute.EntityAttributeModifier.Operation.*;

public class SpecialAttributes {

    private static final UUID SPECIAL_CHEST_ARMOR = UUID.fromString("29ec54a1-98c9-443f-a13a-0c093a0d542f");
    private static final UUID SPECIAL_CHEST_ARMOR_TOUGHNESS = UUID.fromString("07be3346-ecb9-4997-9928-377b869eb893");
    private static final UUID SPECIAL_CHEST_MAX_HEALTH = UUID.fromString("ba0cbbe3-6575-4b4b-ae56-60410af2b73d");
    private static final UUID SPECIAL_CHEST_MOVEMENT_SPEED = UUID.fromString("ba0cbbe3-6575-4b4b-ae56-60410af2b73d");
    private static final UUID SPECIAL_CHEST_ATTACK_DAMAGE = UUID.fromString("7aeb2466-6e51-4cd6-93e5-b2d901f8157d");
    private static final UUID SPECIAL_CHEST_ATTACK_SPEED = UUID.fromString("f6e6ae72-5432-41b9-a56b-0203368d8e97");

    private static final UUID SPECIAL_ATTACK_DAMAGE = UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89");
    private static final UUID SPECIAL_ATTACK_SPEED = UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9");

    public static void removeSpecialAttributes(LivingEntity e) {
        removeAttribute(e, EntityAttributes.GENERIC_ARMOR, SPECIAL_CHEST_ARMOR);
        removeAttribute(e, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, SPECIAL_CHEST_ARMOR_TOUGHNESS);
        removeAttribute(e, EntityAttributes.GENERIC_MAX_HEALTH, SPECIAL_CHEST_MAX_HEALTH);
        removeAttribute(e, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPECIAL_CHEST_MOVEMENT_SPEED);
        removeAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, SPECIAL_CHEST_ATTACK_DAMAGE);
        removeAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, SPECIAL_CHEST_ATTACK_SPEED);

        removeAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, SPECIAL_ATTACK_DAMAGE);
        removeAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, SPECIAL_ATTACK_SPEED);
    }

    public static void applyVoidAttributes(LivingEntity e) {
        addAttribute(e, EntityAttributes.GENERIC_ARMOR, SPECIAL_CHEST_ARMOR, "special_armor", 2.0, ADDITION);
        addAttribute(e, EntityAttributes.GENERIC_ARMOR_TOUGHNESS, SPECIAL_CHEST_ARMOR_TOUGHNESS, "special_armor_toughness", 0.5, MULTIPLY_TOTAL);
        addAttribute(e, EntityAttributes.GENERIC_MAX_HEALTH, SPECIAL_CHEST_MAX_HEALTH, "special_max_health", -0.5, MULTIPLY_TOTAL);
        addAttribute(e, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPECIAL_CHEST_MOVEMENT_SPEED, "special_movement_speed", 0.15, MULTIPLY_TOTAL);
    }

    public static void applyInfernalAttributes(LivingEntity e) {
        addAttribute(e, EntityAttributes.GENERIC_ARMOR, SPECIAL_CHEST_ARMOR, "special_armor", -0.15, MULTIPLY_TOTAL);
        addAttribute(e, EntityAttributes.GENERIC_MAX_HEALTH, SPECIAL_CHEST_MAX_HEALTH, "special_max_health", 1.0, MULTIPLY_TOTAL);
        addAttribute(e, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPECIAL_CHEST_MOVEMENT_SPEED, "special_movement_speed", 0.05, MULTIPLY_TOTAL);
        addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, SPECIAL_CHEST_ATTACK_DAMAGE, "special_attack_damage", 0.15, MULTIPLY_TOTAL);
    }

    public static void applyArchmageAttributes(LivingEntity e) {
        addAttribute(e, EntityAttributes.GENERIC_ARMOR, SPECIAL_CHEST_ARMOR, "special_armor", -0.2, MULTIPLY_TOTAL);
        addAttribute(e, EntityAttributes.GENERIC_MAX_HEALTH, SPECIAL_CHEST_MAX_HEALTH, "special_max_health", -0.1, MULTIPLY_TOTAL);
        addAttribute(e, EntityAttributes.GENERIC_MOVEMENT_SPEED, SPECIAL_CHEST_MOVEMENT_SPEED, "special_movement_speed", 0.05, MULTIPLY_TOTAL);
    }

    public static void applyFishAttributes(LivingEntity e, double baseAttack, double baseSpeed) {
        addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, SPECIAL_ATTACK_DAMAGE, "fish", baseAttack, ADDITION);
        addAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, SPECIAL_ATTACK_SPEED, "fish", baseSpeed, MULTIPLY_TOTAL);
    }

    public static void applyMomentumAttributes(LivingEntity e, double baseAttack, double baseSpeed, int momentumBoost) {
        addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, SPECIAL_ATTACK_DAMAGE, "momentum", baseAttack * momentumBoost, MULTIPLY_TOTAL);
        addAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, SPECIAL_ATTACK_SPEED, "momentum", baseSpeed * momentumBoost, MULTIPLY_TOTAL);
    }

}
