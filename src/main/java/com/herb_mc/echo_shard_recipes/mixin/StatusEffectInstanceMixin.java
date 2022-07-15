package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.StatusEffectInstanceInterface;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements StatusEffectInstanceInterface {

    private boolean boosted = false;

    @Shadow private int amplifier;
    @Shadow int duration;

    @Override
    public void setBoosted(boolean b) {
        boosted = b;
    }

    @Override
    public boolean isBoosted() {
        return boosted;
    }

    @Override
    public void setAmplifier(int a) {
        amplifier = a;
    }

    @Override
    public void setDuration(int d) {
        duration = d;
    }

    @Inject(
            method = "copyFrom",
            at = @At("TAIL")
    )
    private void persistentBoost(StatusEffectInstance that, CallbackInfo ci) {
        this.boosted = ((StatusEffectInstanceInterface)that).isBoosted();
    }

}
