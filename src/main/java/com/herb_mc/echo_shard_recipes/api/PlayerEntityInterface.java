package com.herb_mc.echo_shard_recipes.api;

import net.minecraft.item.ItemStack;

public interface PlayerEntityInterface {

    boolean hasNoCooldown();
    ItemStack getLastStack();

}
