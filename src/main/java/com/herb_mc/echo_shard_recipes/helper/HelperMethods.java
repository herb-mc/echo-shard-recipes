package com.herb_mc.echo_shard_recipes.helper;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class HelperMethods {

    public static String getAttribute(ItemStack i) {
        if (!i.getOrCreateNbt().getBoolean(EchoShardRecipesMod.HAS_ATTRIBUTE)) return "";
        return i.getOrCreateNbt().getString(EchoShardRecipesMod.ATTRIBUTE);
    }

    public static void removeAttribute(LivingEntity entity, EntityAttribute attribute, UUID uuid){
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null && instance.getModifier(uuid) != null)
            instance.removeModifier(uuid);
    }

    public static void addAttribute(LivingEntity e, EchoShardRecipesMod.AttributeItem i) {
        EntityAttributeInstance instance = e.getAttributeInstance(i.attribute);
        if (instance != null && i.uuid != null) {
            EchoShardRecipesMod.LOGGER.info("{}", getAttribute(e.getMainHandStack()));
            instance.addTemporaryModifier(new EntityAttributeModifier(i.uuid, i.tag, i.base, i.op));
        }
    }

    public static void addAttribute(LivingEntity e, EchoShardRecipesMod.AttributeItem i, double base) {
        EntityAttributeInstance instance = e.getAttributeInstance(i.attribute);
        if (instance != null && i.uuid != null)
            instance.addTemporaryModifier(new EntityAttributeModifier(i.uuid, i.tag, base, i.op));
    }

    public static void addAttribute(LivingEntity l, EntityAttribute e, UUID uuid, String tag, double base, EntityAttributeModifier.Operation op) {
        EntityAttributeInstance instance = l.getAttributeInstance(e);
        if (instance != null && uuid != null)
            instance.addTemporaryModifier(new EntityAttributeModifier(uuid, tag, base, op));
    }

    public static void boostStatusEffect(LivingEntity e, StatusEffect s, int amplifier) {
        StatusEffectInstance i = e.getStatusEffect(s);
        boolean addAmp = true;
        if (i == null) {
            addAmp = false;
            e.addStatusEffect(new StatusEffectInstance(s, 4, -1, false, false, false), null);
            i = e.getStatusEffect(s);
            if (i != null) i.applyUpdateEffect(e);
        }
        if (i != null) {
            if (!((StatusEffectInstanceInterface) i).isBoosted()) {
                int a = addAmp ? i.getAmplifier() + amplifier + 1 : i.getAmplifier() + 1;
                i.upgrade(new StatusEffectInstance(s, i.getDuration(), a, i.isAmbient(), i.shouldShowParticles(), i.shouldShowIcon()));
                ((StatusEffectInstanceInterface) i).setBoosted(true);
            }
            i.applyUpdateEffect(e);
            if (i.getAmplifier() == amplifier) ((StatusEffectInstanceInterface) i).setDuration(4);
        }
    }

    public static String id(String s) {
        return EchoShardRecipesMod.MOD_ID + ":" + s;
    }

}
