package com.herb_mc.echo_shard_recipes.mixin;

import net.minecraft.entity.mob.ShulkerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ShulkerEntity.class)
public interface ShulkerEntityAccessor {

    @Invoker("isClosed")
    boolean closed();

}
