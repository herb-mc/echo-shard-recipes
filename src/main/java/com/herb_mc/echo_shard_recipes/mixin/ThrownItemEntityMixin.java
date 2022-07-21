package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.ThrownItemEntityInterface;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownItemEntity.class)
public class ThrownItemEntityMixin implements ThrownItemEntityInterface {

    @Unique private String attribute = "";
    @Unique private float bonus = 0.0f;
    @Unique private float damage = 0.0f;
    @Unique private float increment = 0.0f;

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public void setAttribute(String s) {
        attribute = s;
    }

    @Override
    public void addBonusDamage(float f) {
        bonus += f;
    }

    @Override
    public void setIncrement(float f) {
        increment = f;
    }

    @Override
    public float getIncrement() {
        return increment;
    }

    @Override
    public void setBonusDamage(float f) {
        bonus = f;
    }

    @Override
    public float getBonusDamage() {
        return bonus;
    }

    @Override
    public void setDamage(float f) {
        damage = f;
    }

    @Override
    public float getDamage() {
        return damage;
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void customTimeLimit(NbtCompound nbt, CallbackInfo ci) {
        nbt.putString("attribute", attribute);
        nbt.putFloat("bonus", bonus);
        nbt.putFloat("increment", increment);
        nbt.putFloat("damage", damage);
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void readCustomTimeLimit(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("attribute")) attribute =  nbt.getString("attribute");
        if (nbt.contains("bonus")) bonus =  nbt.getFloat("bonus");
        if (nbt.contains("increment")) increment =  nbt.getFloat("increment");
        if (nbt.contains("damage")) damage =  nbt.getFloat("damage");
    }

}
