package com.herb_mc.echo_shard_recipes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getAttribute;

@Mixin(StatusEffect.class)
public class StatusEffectMixin {

    @Inject(
            method = "applyUpdateEffect",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE",
                    ordinal = 0
            ),
            cancellable = true)
    public void removePoison(LivingEntity entity, int amplifier, CallbackInfo ci) {
        for (ItemStack i : entity.getArmorItems()) if ("anti_corrosive".equals(getAttribute(i))) ci.cancel();
    }

    @Inject(
            method = "applyUpdateEffect",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE",
                    ordinal = 1
            ),
            cancellable = true)
    public void removeWither(LivingEntity entity, int amplifier, CallbackInfo ci) {
        for (ItemStack i : entity.getArmorItems()) if ("anti_corrosive".equals(getAttribute(i))) ci.cancel();
    }

    @Inject(
            method = "applyUpdateEffect",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE",
                    ordinal = 2
            ),
            cancellable = true)
    public void decreaseInstantDamage(LivingEntity entity, int amplifier, CallbackInfo ci) {
        for (ItemStack i : entity.getArmorItems()) if ("anti_corrosive".equals(getAttribute(i))) {
            entity.damage(DamageSource.MAGIC, 0.33f * (float)(6 << amplifier));
            ci.cancel();
        }
    }

    private boolean decreaseDamage = false;

    @Inject(
            method = "Lnet/minecraft/entity/effect/StatusEffect;applyInstantEffect(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;ID)V",
            at = @At("HEAD")
    )
    public void checkIfAntiCorrosive(Entity source, Entity attacker, LivingEntity target, int amplifier, double proximity, CallbackInfo ci) {
        decreaseDamage = false;
        for (ItemStack i : target.getArmorItems()) if ("anti_corrosive".equals(getAttribute(i))) decreaseDamage = true;
    }

    @ModifyArg(
            method = "Lnet/minecraft/entity/effect/StatusEffect;applyInstantEffect(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;ID)V",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE",
                    ordinal = 0
            ),
            index = 1
    )
    public float decreaseDamageNoAttacker(float f) {
        return decreaseDamage ? 0.5f * f : f;
    }

    @ModifyArg(
            method = "Lnet/minecraft/entity/effect/StatusEffect;applyInstantEffect(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;ID)V",
            at = @At(
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE",
                    ordinal = 1
            ),
            index = 1
    )
    public float decreaseDamage(float f) {
        return decreaseDamage ? 0.5f * f : f;
    }

}
