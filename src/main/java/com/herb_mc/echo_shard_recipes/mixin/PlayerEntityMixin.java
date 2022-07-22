package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.LivingEntityInterface;
import com.herb_mc.echo_shard_recipes.api.ManaPlayer;
import com.herb_mc.echo_shard_recipes.api.PlayerEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.getAttribute;
import static com.herb_mc.echo_shard_recipes.helper.Entities.isInorganic;
import static com.herb_mc.echo_shard_recipes.helper.ItemHelper.getNearestItems;
import static com.herb_mc.echo_shard_recipes.helper.Spells.tickMana;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityInterface, ManaPlayer {

    @Unique private Entity target = null;
    @Unique private boolean hasNoCooldown = false;
    @Unique private ItemStack lastStack = ItemStack.EMPTY;
    @Unique private ServerBossBar bossBar = new ServerBossBar(Text.of("Mana"), BossBar.Color.BLUE, BossBar.Style.PROGRESS);
    @Unique private double mana = 15.0;
    @Unique private final double maxMana = 15.0;
    @Unique private double maxManaBoost = 0.0;
    @Unique private final double manaRegenRate = 0.015;
    @Unique private double manaRegenBoost= 0.0;

    @Override
    public boolean hasNoCooldown() {
        return hasNoCooldown;
    }

    @Override
    public ItemStack getLastStack() {
        return lastStack;
    }

    @Override
    public double getMana() {
        return mana;
    }

    @Override
    public boolean tryRemoveMana(double d) {
        if (((PlayerEntity) (Object) this).isCreative()) return true;
        else if (mana - d > 0) {
            mana -= d;
            return true;
        }
        else return false;
    }

    @Override
    public void addMana(double d) {
        mana = Math.min(mana + d, maxMana + maxManaBoost);
    }

    @Override
    public void setMana(double d) {
        mana = Math.min(d, maxMana + maxManaBoost);
    }

    @Override
    public void addManaRegenBoost(double d) {
        manaRegenBoost += d;
    }

    @Override
    public void setManaRegenBoost(double d) {
        manaRegenBoost = d;
    }

    @Override
    public double getManaRegenBoost(double d) {
        return manaRegenBoost;
    }

    @Override
    public double getManaRegen() {
        return manaRegenBoost + manaRegenRate;
    }

    @Override
    public double getMaxMana() {
        return maxMana + maxManaBoost;
    }

    @Inject(
            method = "attack",
            at = @At("HEAD")
    )
    public void getTarget(Entity target, CallbackInfo ci) {
        this.target = target;
    }

    @ModifyArg(
            method = "attack",
            at = @At(
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE"
            )
    )
    public float modifyDamage(float f) {
        PlayerEntity p = (PlayerEntity) (Object) this;
        if (target instanceof LivingEntity) switch(getAttribute(p.getMainHandStack())) {
                case "flowing_water" -> {
                    if (((LivingEntity) target).hurtTime > 0 && ((LivingEntity) target).hurtTime < 9) {
                        ((LivingEntity) target).hurtTime = 0;
                        target.timeUntilRegen = 1;
                        if (((LivingEntity) target).hurtTime < 6) f *= 1.6;
                    }
                }
                case "crushing_wave" -> {
                    if (((LivingEntity) target).getHealth() == ((LivingEntity) target).getMaxHealth()) {
                        f *= 1.5;
                        ((LivingEntity) target).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 50, 2));
                    }
                }
                case "firebrand" -> {
                    f *= p.isOnFire() ? 1.1f : 1;
                    f *= target.isOnFire() ? 1.2f : 1;
                }
                case "stonebreaker" -> f *= isInorganic((LivingEntity) target) ? 4 : 1;
                default -> {}
            }
        return f;
    }

    @Inject(
            method = "attack",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;getHealth()F",
                    value = "INVOKE"
            )
    )
    public void momentumBoost(Entity target, CallbackInfo ci) {
        if ("momentum".equals(getAttribute(((LivingEntity) (Object) this).getMainHandStack())) && ((LivingEntityInterface) this).getMomentum() < 5) ((LivingEntityInterface) this).addMomentum();
    }

    @ModifyArg(
            method = "attack",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V",
                    value = "INVOKE"
            ),
            index = 0
    )
    public double modifyKnockback(double d) {
        PlayerEntity p = (PlayerEntity) (Object) this;
        switch(getAttribute(p.getMainHandStack())) {
            case "flowing_water", "crushing_wave" -> d *= 0;
            default -> {}
        }
        return d;
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void handleTickEvents(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!player.world.isClient) {
            tickMana(player, bossBar);
            hasNoCooldown = player.getAttackCooldownProgress(0.5f) >= 1;
            if (lastStack != player.getMainHandStack()) hasNoCooldown = false;
            lastStack = player.getMainHandStack();
            if ("magnetized".equals(getAttribute(player.getMainHandStack())))
                for (ItemEntity i : getNearestItems(player, 6.0)) {
                    Vec3d p = player.getPos().subtract(i.getPos()).normalize().multiply(0.1);
                    Vec3d v = i.getVelocity().add(p);
                    if (v.lengthSquared() > 0.25) v = p.multiply(5);
                    i.setVelocity(v);
                    i.velocityDirty = true;
                    i.velocityModified = true;
                }
        }
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("HEAD")
    )
    private void writeToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putDouble("Mana", mana);
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("HEAD")
    )
    private void readFromNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("Mana")) mana = nbt.getDouble("Mana");
    }

}
