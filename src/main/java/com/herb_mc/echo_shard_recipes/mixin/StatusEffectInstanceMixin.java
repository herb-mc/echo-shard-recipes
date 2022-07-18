package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.StatusEffectInstanceInterface;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements StatusEffectInstanceInterface {

    @Unique private boolean boosted = false;
    @Unique private int levelBoost = 0;

    @Shadow private int amplifier;
    @Shadow int duration;

    @Override
    public StatusEffectInstance setAmplifier(int a) {
        amplifier = a;
        return (StatusEffectInstance) (Object) this;
    }

    @Override
    public StatusEffectInstance setDuration(int d) {
        duration = d;
        return (StatusEffectInstance) (Object) this;
    }

    @Override
    public StatusEffectInstance setLevelBoost(int d) {
        levelBoost = d;
        return (StatusEffectInstance) (Object) this;
    }

    @Override
    public StatusEffectInstance setBoosted(boolean b) {
        boosted = b;
        return (StatusEffectInstance) (Object) this;
    }

    @Override
    public boolean isBoosted() {
        return boosted;
    }

    @Override
    public int getLevelBoost() {
        return levelBoost;
    }
/*
    @Inject(
            method = "Lnet/minecraft/entity/effect/StatusEffectInstance;writeTypelessNbt(Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At("TAIL")
    )
    private void writeToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("LevelBoost", levelBoost);
        nbt.putBoolean("Boosted", boosted);
    }

    @Inject(
            method = "fromNbt(Lnet/minecraft/entity/effect/StatusEffect;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/effect/StatusEffectInstance;",
            at = @At("TAIL")
    )
    private static void getFromNbt(StatusEffect type, NbtCompound nbt, CallbackInfoReturnable<StatusEffectInstance> cir) {
        ((StatusEffectInstanceInterface) cir.getReturnValue()).setLevelBoost(nbt.getInt("LevelBoost"));
        ((StatusEffectInstanceInterface) cir.getReturnValue()).setBoosted(nbt.getBoolean("Boosted"));
    }
 */

}
