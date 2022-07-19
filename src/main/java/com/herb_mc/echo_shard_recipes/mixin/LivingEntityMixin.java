package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.helper.LivingEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.ThrownItemEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
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

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;
import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityInterface {

    @Shadow protected int riptideTicks;

    @Unique private int flightTime = 0;
    @Unique private int dealtDamageTime = 0;
    @Unique private int momentumBoost = 0;
    @Unique private int reflexBoost = 0;
    @Unique private DamageSource source;

    @Override
    public void addMomentum() {
        momentumBoost++;
        dealtDamageTime = 50;
    }

    @Override
    public int getMomentum() {
        return momentumBoost;
    }

    @Inject(
            method = "travel",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private void resetFlight(Vec3d movementInput, CallbackInfo ci) {
        if (!((LivingEntity) (Object) this).isFallFlying()) flightTime = 0;
    }

    @Inject(
            method = "travel",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private void elytraParticles(Vec3d movementInput, CallbackInfo ci) {
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
    private void applyAttributes(CallbackInfo ci) {
        LivingEntity e = (LivingEntity) (Object) this;
        if (!e.world.isClient()) {
            Map<String, EchoShardRecipesMod.AttributeItem> items = EchoShardRecipesMod.ATTRIBUTE_ITEMS;
            for (EchoShardRecipesMod.AttributeItem i : items.values())
                if (i.attribute != null) removeAttribute(e, i.attribute, i.uuid);
            removeAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89"));
            removeAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9"));
            removeAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("401ef5aa-ea51-4964-ab92-800cd8a39d89"));
            removeAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d14cff35b9"));
            int numStatus = 0;
            switch (getAttribute(e.getMainHandStack())) {
                case "light", "sharpened", "stonebreaker", "terraforming" -> addAttribute(e, items.get(getAttribute(e.getMainHandStack())));
                case "rip_current" -> {
                    addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89"), "fish", 7.0, EntityAttributeModifier.Operation.ADDITION);
                    addAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9"), "fish", -0.5, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
                }
                case "flowing_water" -> {
                    addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89"), "fish", 4.0, EntityAttributeModifier.Operation.ADDITION);
                    addAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9"), "fish", -0.3, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
                }
                case "crushing_wave" -> {
                    addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800bd8a39d89"), "fish", 11.0, EntityAttributeModifier.Operation.ADDITION);
                    addAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d11cff35b9"), "fish", -0.85, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
                }
                case "alchemist" -> {
                    numStatus = e.getActiveStatusEffects().size();
                }
                default -> {
                }
            }
            if (dealtDamageTime > 0) dealtDamageTime--;
            if (dealtDamageTime <= 0 && momentumBoost > 0) {
                momentumBoost--;
                dealtDamageTime = 50;
            }
            if (momentumBoost > 0) {
                addAttribute(e, EntityAttributes.GENERIC_ATTACK_DAMAGE, UUID.fromString("401ef5aa-ea51-4964-ab92-800cd8a39d89"), "momentum", 0.07 * momentumBoost, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
                addAttribute(e, EntityAttributes.GENERIC_ATTACK_SPEED, UUID.fromString("231b1cf0-82ff-4432-b939-f2d14cff35b9"), "momentum", 0.05 * momentumBoost, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
            }
            double armor = 0.0;
            double moveSpeed = 0.0;
            double toughness = 0.0;
            double health = 0.0;
            double knockbackRes = 0.0;
            for (ItemStack i : e.getArmorItems())
                switch (getAttribute(i)) {
                    case "snipe_shot" -> addAttribute(e, items.get(getAttribute(i)));
                    case "reflex" -> {
                        if (reflexBoost > 0) {
                            addAttribute(e, items.get(getAttribute(i)));
                            reflexBoost--;
                        }
                    }
                    case "reinforced" -> armor += items.get("reinforced").base;
                    case "swift" -> moveSpeed += items.get("swift").base;
                    case "steady_body" -> {
                        if (e.isSneaking()) {
                            addAttribute(e, items.get("steady_body"));
                            e.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 4, 0, false, false, false), null);
                        }
                    }
                    case "levitator" -> {
                        if (e.isSneaking())
                            e.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 4, 3, false, false, false), null);
                    }
                    case "featherweight" -> {
                        if (e.isSneaking())
                            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 4, 0, false, false, false), null);
                    }
                    case "resilient" -> toughness += items.get("resilient").base;
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
                if (moveSpeed == items.get("swift").base * 4) moveSpeed += 0.8;
                addAttribute(e, items.get("swift"), moveSpeed);
            }
            if (toughness > 0) {
                if (toughness == items.get("resilient").base * 4) toughness += 4;
                addAttribute(e, items.get("resilient"), toughness);
            }
            if (health > 0) {
                if (toughness == items.get("rejuvenating").base * 4) health += 2;
                addAttribute(e, items.get("rejuvenating"), health);
            }
            if (numStatus > 0) addAttribute(e, items.get("alchemist"), numStatus);
            if (knockbackRes > 0) addAttribute(e, items.get("stalwart"), knockbackRes);
        }
    }

    @Inject(
            method = "damage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyDamageEffects(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.source = source;
        for (ItemStack i : ((LivingEntity) (Object) this).getArmorItems()) if ("turtle_shell".equals(getAttribute(i)) && (source == DamageSource.CACTUS || source == DamageSource.FREEZE || source == DamageSource.ON_FIRE || source == DamageSource.HOT_FLOOR || source == DamageSource.LAVA || source == DamageSource.SWEET_BERRY_BUSH)) cir.setReturnValue(false);
        if (source.getAttacker() != null) reflexBoost = 40;
    }

    @ModifyArg(
            method = "damage",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V",
                    value = "INVOKE"
            ),
            index = 0
    )
    private double modifyKnockback(double d) {
        if (source instanceof EntityDamageSource && source.getAttacker() instanceof LivingEntity) switch (getAttribute(((LivingEntity) source.getAttacker()).getMainHandStack())) {
                case "flowing_water" -> d *= 0.6;
                case "crushing_wave" -> d *= 0;
                default -> {}
            }
        Entity e = source.getSource();
        if (e instanceof ThrownItemEntity && "flamethrower".equals(((ThrownItemEntityInterface) e).getAttribute())) d *= 0.08;
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
            int strengthBoost = 0;
            int resistance = 0;
            int jumpBoost = 0;
            if (reflexBoost > 0) resistance++;
            if ("indomitable".equals(getAttribute(e.getMainHandStack())) && e.isBlocking()) resistance++;
            else if ("indomitable".equals(getAttribute(e.getOffHandStack())) && e.isBlocking()) resistance++;
            switch (getAttribute(e.getMainHandStack())) {
                case "hasty" -> hasteBoost += 1;
                case "terraforming" -> hasteBoost += 15;
                case "excavator" -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 4, e.isSneaking() ? 1 : 0, true, false, false));
                default -> {}
            }
            for (ItemStack i : e.getArmorItems()) switch (getAttribute(i)) {
                case "power_assist" -> {
                    if (TOOL.isValidItem(e.getMainHandStack().getItem())) {
                        hasteBoost++;
                        strengthBoost++;
                    }
                }
                case "machine_assist" -> {
                    jumpBoost++;
                    if (e.isSneaking()) jumpBoost++;
                }
                default -> {}
            }
            if (hasteBoost > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 4, -1 + hasteBoost, true, false, false));
            if (strengthBoost > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 4, -1 + strengthBoost, true, false, false));
            if (resistance > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 4, -1 + resistance, true, false, false));
            if (jumpBoost > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 4, -1 + jumpBoost, true, false, false));

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

    @Inject(
            method = "takeShieldHit",
            at = @At("HEAD")
    )
    private void addRevengeBuff(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity e = (LivingEntity) (Object) this;
        if ("revenge".equals(getAttribute(e.getMainHandStack())) || "revenge".equals(getAttribute(e.getOffHandStack()))) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 30, 0, true, false, false));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30, 0, true, false, false));
        }
    }

}
