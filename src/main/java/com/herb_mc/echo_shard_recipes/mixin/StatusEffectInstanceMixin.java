package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.StatusEffectInstanceInterface;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectInstance.class)
public class StatusEffectInstanceMixin implements StatusEffectInstanceInterface {

    @Unique private boolean equipBoosted = false;
    @Unique private boolean armorBoosted = false;

    @Shadow private int amplifier;
    @Shadow int duration;

    @Override
    public void setEquipBoosted(boolean b) {
        equipBoosted = b;
    }

    @Override
    public boolean isEquipBoosted() {
        return equipBoosted;
    }

    @Override
    public void setArmorBoosted(boolean b) {
        armorBoosted = b;
    }

    @Override
    public boolean isArmorBoosted() {
        return armorBoosted;
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
        this.equipBoosted = ((StatusEffectInstanceInterface)that).isEquipBoosted();
        this.armorBoosted = ((StatusEffectInstanceInterface)that).isArmorBoosted();
    }

}
