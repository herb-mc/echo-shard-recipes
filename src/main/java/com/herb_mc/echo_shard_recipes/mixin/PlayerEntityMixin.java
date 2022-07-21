package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.LivingEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.PlayerEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getAttribute;
import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.isInorganic;
import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getNearestItems;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityInterface {

    @Unique private Entity target = null;
    @Unique private boolean canShoot = false;
    @Unique private ItemStack lastStack = ItemStack.EMPTY;

    @Override
    public boolean canShoot() {
        return canShoot;
    }

    @Override
    public ItemStack getLastStack() {
        return lastStack;
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
                    if (((LivingEntity) target).hurtTime > 0) {
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

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void magnetizeItems(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        canShoot = player.getAttackCooldownProgress(0.5f) >= 1;
        if (lastStack != player.getMainHandStack()) canShoot = false;
        lastStack = player.getMainHandStack();
        if (!player.world.isClient && "magnetized".equals(getAttribute(player.getMainHandStack())))
            for (ItemEntity i : getNearestItems(player, 6.0)) {
                Vec3d p = player.getPos().subtract(i.getPos()).normalize().multiply(0.1);
                Vec3d v = i.getVelocity().add(p);
                if (v.lengthSquared() > 0.25) v = p.multiply(5);
                i.setVelocity(v);
                i.velocityDirty = true;
                i.velocityModified = true;
            }
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

}
