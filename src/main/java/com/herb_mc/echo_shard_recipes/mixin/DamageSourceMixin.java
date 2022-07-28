package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.DamageSourceInterface;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements DamageSourceInterface {

    @Unique private double armorPierce = 0;

    @Override
    public void setArmorPierce(double d) {
        armorPierce = d;
    }

    @Override
    public double getArmorPierce() {
        return armorPierce;
    }

}
