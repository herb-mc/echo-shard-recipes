package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.ThrownItemEntityInterface;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ThrownItemEntity.class)
public class ThrownItemEntityMixin implements ThrownItemEntityInterface {

    @Unique private String attribute = null;
    @Unique private float bonus = 0.0f;

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public void setAttribute(String s) {
        attribute = s;
    }

    @Override
    public void setBonusDamage(float f) {
        bonus = f;
    }

    @Override
    public float getBonusDamage() {
        return bonus;
    }

}
