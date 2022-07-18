package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.PARTICLE_ITEMS;
import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow protected int riptideTicks;

    @Unique private int flightTime = 0;
    @Unique private DamageSource source;

    @Inject(
            method = "travel",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    public void resetFlight(Vec3d movementInput, CallbackInfo ci) {
        if (!((LivingEntity) (Object) this).isFallFlying()) flightTime = 0;
    }

    @Inject(
            method = "travel",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    public void elytraParticles(Vec3d movementInput, CallbackInfo ci) {
        flightTime++;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (flightTime >= 10 && entity.world instanceof ServerWorld && this.riptideTicks <= 0) {
            for (ItemStack i: entity.getArmorItems()) {
                NbtCompound nbt = i.getNbt();
                if (nbt != null && nbt.getBoolean("HasShardParticleEffect")){
                    EchoShardRecipesMod.ParticleItem p = PARTICLE_ITEMS[nbt.getInt("ShardParticleEffect")];
                    float pitch = entity.getPitch(1.0F) * 0.017453292F;
                    Vec3d rot =  entity.getRotationVector().normalize();
                    Vec3d v = entity.getRotationVector().crossProduct(new Vec3d(0, 1, 0)).normalize();
                    double mul = 0.3 * (1 - Math.pow(Math.PI / - (Math.acos(rot.dotProduct(new Vec3d(0, pitch, 0).normalize())) - 1.5 * Math.PI) - 1, 3)) ;
                    double x = entity.getX() - 0.5 * rot.x;
                    double y = entity.getY() - 0.5 * rot.y;
                    double z = entity.getZ() - 0.5 * rot.z;
                    spawnParticles((ServerWorld) entity.world, p.particle, x + mul * v.x, y + mul * v.y, z + mul * v.z, 1, 0, 0, 0, 0);
                    spawnParticles((ServerWorld) entity.world, p.particle, x - mul * v.x, y - mul * v.y, z - mul * v.z, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    public void applyAttributes(CallbackInfo ci) {
        LivingEntity e = (LivingEntity) (Object) this;
        Map<String, EchoShardRecipesMod.AttributeItem> items = EchoShardRecipesMod.ATTRIBUTE_ITEMS;
        for (EchoShardRecipesMod.AttributeItem i : items.values())
            if (i.attribute != null) removeAttribute(e, i.attribute, i.uuid);
        removeAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89"));
        removeAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9"));
        switch (getAttribute(e.getMainHandStack())) {
            case "light", "sharpened", "stonebreaker", "terraforming" -> addAttribute(e, items.get(getAttribute(e.getMainHandStack())));
            case "rip_current" -> {
                addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89"),  "echo_shard_recipes:fish", 7.0, EntityAttributeModifier.Operation.ADDITION);
                addAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9"),  "echo_shard_recipes:fish", -0.5, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case "flowing_water" -> {
                addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89"),  "echo_shard_recipes:fish", 4.0, EntityAttributeModifier.Operation.ADDITION);
                addAttribute(e,EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9"),  "echo_shard_recipes:fish", -0.3, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            case "crushing_wave" -> {
                addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89"),  "echo_shard_recipes:fish", 11.0, EntityAttributeModifier.Operation.ADDITION);
                addAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9"),  "echo_shard_recipes:fish", -0.85, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            default -> {}
        }
        double armor = 0.0;
        double moveSpeed = 0.0;
        double toughness = 0.0;
        double health = 0.0;
        double knockbackRes = 0.0;
        for (ItemStack i : e.getArmorItems()) switch (getAttribute(i)) {
                case "snipe_shot" -> addAttribute(e, items.get("snipe_shot"));
                case "reinforced" -> armor += items.get("reinforced").base;
                case "swift" -> moveSpeed += items.get("swift").base;
                case "levitator" -> { if (e.isSneaking()) e.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 4, 3, false, false, false), null); }
                case "featherweight" -> { if (e.isSneaking()) e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 4, 0, false, false, false), null); }
                case "resilient" -> toughness += items.get(":resilient").base;
                case "rejuvenating" -> health += items.get("rejuvenating").base;
                case "stalwart" -> knockbackRes += items.get("stalwart").base;
                default -> {
                }
            }
        if (armor > 0) {
            if (armor == items.get("reinforced").base * 4) armor += 2;
            addAttribute(e, items.get("reinforced"), armor);
        }
        if (moveSpeed > 0) {
            if (moveSpeed == items.get("swift").base * 4) moveSpeed += 0.4;
            addAttribute(e, items.get("swift"), moveSpeed);
        }
        if (toughness > 0) addAttribute(e, items.get("resilient"), toughness);
        if (health > 0) addAttribute(e, items.get("rejuvenating"), health);
        if (knockbackRes > 0) addAttribute(e, items.get("stalwart"), knockbackRes);
    }

    @Inject(
            method = "damage",
            at = @At("HEAD")
    )
    public void getSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.source = source;
    }

    @ModifyArg(
            method = "damage",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V",
                    value = "INVOKE"
            ),
            index = 0
    )
    public double modifyKnockback(double d) {
        if (source instanceof EntityDamageSource && source.getAttacker() instanceof LivingEntity) switch (getAttribute(((LivingEntity) source.getAttacker()).getMainHandStack())) {
                case "flowing_water" -> d *= 0.6;
                case "crushing_wave" -> d *= 0;
                default -> {}
            }
        return d;
    }

    @Inject(
            method = "tickStatusEffects",
            at = @At("HEAD")
    )
    private void addStatus(CallbackInfo ci) {
        LivingEntity e = (LivingEntity) (Object) this;
        if (!e.world.isClient()) {
            int hasteBoost = 0;
            switch (getAttribute(e.getMainHandStack())) {
                case "hasty" -> hasteBoost += 1;
                case "terraforming" -> hasteBoost += 15;
                case "excavator" -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 4, e.isSneaking() ? 1 : 0, true, false, false));
                default -> {}
            }
            for (ItemStack i : e.getArmorItems()) switch (getAttribute(i)) {
                case "power_assist" -> hasteBoost += 1;
                default -> {}
            }
            if (hasteBoost > 0) {
                StatusEffectInstance s = new StatusEffectInstance(StatusEffects.HASTE, 4, -1 + hasteBoost, true, false, false);
                e.addStatusEffect(s);
            }
            /*
            TODO
            if (hasteBoost > 0 && !((StatusEffectInstanceInterface) s).isBoosted()) {
                LOGGER.info("not boosted case");
                ((StatusEffectInstanceInterface) s).setBoosted(true);
                ((StatusEffectInstanceInterface) s).setLevelBoost(hasteBoost);
                ((StatusEffectInstanceInterface) s).setAmplifier(s.getAmplifier() + hasteBoost);
                e.addStatusEffect(s);
            }
            else if (hasteBoost > 0 && ((StatusEffectInstanceInterface) s).isBoosted() && s.getAmplifier() == hasteBoost - 1) {
                LOGGER.info("maintain case");
                ((StatusEffectInstanceInterface) s).setDuration(4);
            }
            else if (hasteBoost != ((StatusEffectInstanceInterface) s).getLevelBoost()) {
                LOGGER.info("level change case {} {}", hasteBoost, ((StatusEffectInstanceInterface) s).getLevelBoost());
                s = ((StatusEffectInstanceInterface)new StatusEffectInstance(StatusEffects.HASTE, 4, s.getAmplifier() + hasteBoost - ((StatusEffectInstanceInterface) s).getLevelBoost(), true, false, false)).setBoosted(true);
                ((StatusEffectInstanceInterface) s).setLevelBoost(hasteBoost);
                if (hasteBoost == 0) ((StatusEffectInstanceInterface) s).setBoosted(false);
            }
             */
        }
    }

}
