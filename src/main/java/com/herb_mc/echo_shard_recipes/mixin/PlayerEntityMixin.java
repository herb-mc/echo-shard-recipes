package com.herb_mc.echo_shard_recipes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getAttribute;
import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.isInorganic;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    private Entity target = null;

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
        if (target instanceof LivingEntity)
            switch(getAttribute(p.getMainHandStack())) {
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
                case "firebrand" -> f *= target.isOnFire() ? 1.25f : 1;
                case "stonebreaker" -> f *= isInorganic((LivingEntity) target) ? 4 : 1;
                default -> {}
            }
        return f;
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
