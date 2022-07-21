package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.api.LivingEntityInterface;
import com.herb_mc.echo_shard_recipes.api.ThrownItemEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.Attributes;
import com.herb_mc.echo_shard_recipes.helper.Items;
import com.herb_mc.echo_shard_recipes.helper.Projectiles;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;
import static com.herb_mc.echo_shard_recipes.helper.Network.*;
import static com.herb_mc.echo_shard_recipes.helper.SpecialAttributes.*;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityInterface {

    @Shadow protected int riptideTicks;

    @Unique private boolean using = false;
    @Unique private int dealtDamageTime = 0;
    @Unique private int momentumBoost = 0;
    @Unique private int reflexBoost = 0;
    @Unique private int burstTimer = 0;
    @Unique private ItemStack currentStack = ItemStack.EMPTY;
    @Unique private DamageSource source;

    @Override
    public void addMomentum() {
        momentumBoost++;
        dealtDamageTime = 50;
    }

    @Override
    public void setUsing(boolean b) {
        using = b;
    }

    @Override
    public void setBurst(int i, ItemStack itemStack) {
        burstTimer = i;
        currentStack = itemStack;
    }

    @Override
    public int getMomentum() {
        return momentumBoost;
    }

    @Override
    public boolean getUsing() {
        return using;
    }

    @Inject(
            method = "travel",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;getVelocity()Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private void elytraParticles(Vec3d movementInput, CallbackInfo ci) {
        if (!((LivingEntity) (Object) this).world.isClient() && this.riptideTicks <= 0 && ((LivingEntity) (Object) this).isFallFlying()) {
            LivingEntity entity = (LivingEntity) (Object) this;
            for (ItemStack i: entity.getArmorItems()) {
                NbtCompound nbt = i.getNbt();
                if (nbt != null && nbt.getBoolean(HAS_PARTICLE)){
                    EchoShardRecipesMod.ParticleItem p = PARTICLE_ITEMS[nbt.getInt(PARTICLE)];
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
        if (!((LivingEntity) (Object) this).world.isClient()) {
            LivingEntity e = (LivingEntity) (Object) this;
            using = false;
            if (e instanceof PlayerEntity user) {
                boolean c = user.isCreative();
                if (burstTimer > 0) {
                    burstTimer--;
                    using = true;
                }
                if (currentStack != ItemStack.EMPTY && burstTimer % 2 == 0 && e.getMainHandStack() == currentStack) {
                    ItemStack ammo = Items.hasItem(user.getInventory(), net.minecraft.item.Items.GOLD_NUGGET);
                    if (ammo != null || c) {
                        if (!c) currentStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
                        playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
                        playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
                        ((ThrownItemEntityInterface) Projectiles.createBullet(user.world, user, 4.0f, 0.0174533, 5.0f)).setIncrement(1.0f);
                        Vec3d rot = user.getRotationVector().multiply(-0.05);
                        user.addVelocity(rot.x, rot.y, rot.z);
                        if (user instanceof ServerPlayerEntity && !user.world.isClient())
                            ((ServerPlayerEntity) user).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                        if (!c) Items.decrement(ammo);
                    } else {
                        currentStack = ItemStack.EMPTY;
                        burstTimer = 0;
                    }
                } else if (e.getMainHandStack() != currentStack) {
                    currentStack = ItemStack.EMPTY;
                    burstTimer = 0;
                }
                if (burstTimer <= 0) currentStack = ItemStack.EMPTY;
            }
            Map<String, EchoShardRecipesMod.AttributeItem> items = EchoShardRecipesMod.ATTRIBUTE_ITEMS;
            for (EchoShardRecipesMod.AttributeItem i : items.values())
                if (i.attribute != null) Attributes.removeAttribute(e, i.attribute, i.uuid);
            removeSpecialAttributes(e);
            int numStatus = 0;
            switch (Attributes.getAttribute(e.getMainHandStack())) {
                case "light", "sharpened", "stonebreaker", "terraforming" -> Attributes.addAttribute(e, items.get(Attributes.getAttribute(e.getMainHandStack())));
                case "rip_current" -> applyFishAttributes(e, 7.0, -0.5);
                case "flowing_water" -> applyFishAttributes(e, 4.0, -0.25);
                case "crushing_wave" -> applyFishAttributes(e, 1.0, -0.85);
                case "alchemist" -> numStatus = e.getActiveStatusEffects().size();
                case "gun_ho" -> {
                    Item i = e.getMainHandStack().getItem();
                    if (i == net.minecraft.item.Items.WOODEN_HOE || i == net.minecraft.item.Items.GOLDEN_HOE) Attributes.addAttribute(e, items.get("gun_ho"), -0.5);
                    else if (i == net.minecraft.item.Items.STONE_HOE || i == net.minecraft.item.Items.IRON_HOE)
                        Attributes.addAttribute(e, items.get("gun_ho"), -0.67);
                    else if (i == net.minecraft.item.Items.DIAMOND_HOE || i == net.minecraft.item.Items.NETHERITE_HOE)
                        Attributes.addAttribute(e, items.get("gun_ho"), -0.75);
                }
                default -> {
                }
            }
            if (dealtDamageTime > 0) dealtDamageTime--;
            if (dealtDamageTime <= 0 && momentumBoost > 0) {
                momentumBoost--;
                dealtDamageTime = 50;
            }
            if (momentumBoost > 0) applyMomentumAttributes(e, 0.07, 0.05, momentumBoost);
            double armor = 0.0;
            double moveSpeed = 0.0;
            double toughness = 0.0;
            double health = 0.0;
            double knockbackRes = 0.0;
            for (ItemStack i : e.getArmorItems())
                switch (Attributes.getAttribute(i)) {
                    case "snipe_shot" -> Attributes.addAttribute(e, items.get(Attributes.getAttribute(i)));
                    case "heat_conductor" -> {
                        if (e.isOnFire()) Attributes.addAttribute(e, items.get(Attributes.getAttribute(i)));
                    }
                    case "reflex" -> {
                        if (reflexBoost-- > 0) Attributes.addAttribute(e, items.get(Attributes.getAttribute(i)));
                    }
                    case "reinforced" -> armor += items.get("reinforced").base;
                    case "swift" -> moveSpeed += items.get("swift").base;
                    case "steady_body" -> {
                        if (e.isSneaking()) {
                            Attributes.addAttribute(e, items.get("steady_body"));
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
                    case "voided" -> applyVoidAttributes(e);
                    case "infernal" -> applyInfernalAttributes(e);
                    default -> {
                    }
                }
            if (armor > 0)
                Attributes.addAttribute(e, items.get("reinforced"), (armor == items.get("reinforced").base * 4) ? armor + 2 : armor);
            if (moveSpeed > 0)
                Attributes.addAttribute(e, items.get("swift"), (moveSpeed == items.get("swift").base * 4) ? moveSpeed + 0.08 : moveSpeed);
            if (toughness > 0)
                Attributes.addAttribute(e, items.get("resilient"), (toughness == items.get("resilient").base * 4) ? toughness + 4 : toughness);
            if (health > 0)
                Attributes.addAttribute(e, items.get("rejuvenating"), (toughness == items.get("rejuvenating").base * 4) ? health + 2 : health);
            if (numStatus > 0) Attributes.addAttribute(e, items.get("alchemist"), numStatus);
            if (knockbackRes > 0) Attributes.addAttribute(e, items.get("stalwart"), knockbackRes);

            int hasteBoost = 0;
            int strengthBoost = 0;
            int resistance = 0;
            int jumpBoost = 0;
            int slowness = 0;
            if (reflexBoost > 0) resistance++;
            if ("indomitable".equals(Attributes.getAttribute(e.getMainHandStack())) && e.isBlocking()) resistance++;
            else if ("indomitable".equals(Attributes.getAttribute(e.getOffHandStack())) && e.isBlocking()) resistance++;
            switch (Attributes.getAttribute(e.getMainHandStack())) {
                case "hasty" -> hasteBoost += 1;
                case "terraforming" -> hasteBoost += 15;
                case "excavator" -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 4, e.isSneaking() ? 1 : 0, true, false, false));
                case "gun_ho" -> {if (e.getMainHandStack().getItem() == net.minecraft.item.Items.WOODEN_HOE && e.isSneaking()) slowness += 60;}
                default -> {}
            }
            for (ItemStack i : e.getArmorItems()) switch (Attributes.getAttribute(i)) {
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
            if (hasteBoost > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 2, hasteBoost - 1, true, false, false));
            if (strengthBoost > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 2, -1 + strengthBoost, true, false, false));
            if (resistance > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 2, -1 + resistance, true, false, false));
            if (jumpBoost > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 2, -1 + jumpBoost, true, false, false));
            if (slowness > 0) e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2, -1 + slowness, true, false, false));
        }
    }

    @Inject(
            method = "damage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void applyDamageEffects(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity e = ((LivingEntity) (Object) this);
        if (!e.world.isClient()) {
            this.source = source;
            for (ItemStack i : e.getArmorItems()) if ("turtle_shell".equals(Attributes.getAttribute(i)) && (source == DamageSource.CACTUS || source == DamageSource.FREEZE || source == DamageSource.ON_FIRE || source == DamageSource.HOT_FLOOR || source == DamageSource.LAVA || source == DamageSource.SWEET_BERRY_BUSH))
                    cir.setReturnValue(false);
            if (source.getAttacker() != null) reflexBoost = 40;
        }
    }

    @ModifyVariable(
            method = "damage",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z",
                    value = "INVOKE"
            ),
            index = 2,
            argsOnly = true
    )
    private float applyDamageMods(float value) {
        for (ItemStack i : ((LivingEntity) (Object) this).getArmorItems()) switch (Attributes.getAttribute(i)) {
            case "voided" -> value *= 0.65;
            case "infernal" -> {
                if (source.getAttacker() != null) {
                    float f = ECHO_SHARD_RANDOM.nextFloat();
                    if (f < 0.05) value *= 2.5f;
                    else if (f < 0.015) value *= 2.0f;
                    else value *= 1.4;
                }
            }
            default -> {}
        }
        return value;
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
        if (source instanceof EntityDamageSource && source.getAttacker() instanceof LivingEntity) switch (Attributes.getAttribute(((LivingEntity) source.getAttacker()).getMainHandStack())) {
                case "flowing_water" -> d *= 0.6;
                case "crushing_wave" -> d *= 0;
                default -> {}
            }
        Entity e = source.getSource();
        if (e instanceof ThrownItemEntity && "flamethrower".equals(((ThrownItemEntityInterface) e).getAttribute())) d *= 0.08;
        return d;
    }

    @Inject(
            method = "takeShieldHit",
            at = @At("HEAD")
    )
    private void addRevengeBuff(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity e = (LivingEntity) (Object) this;
        if ("revenge".equals(Attributes.getAttribute(e.getMainHandStack())) || "revenge".equals(Attributes.getAttribute(e.getOffHandStack()))) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 30, 0, true, false, false));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30, 0, true, false, false));
        }
    }

}
