package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.item.ItemStack;

public interface LivingEntityInterface {

    void addMomentum();
    void setBurst(int i, ItemStack itemStack);
    int getMomentum();
    void setUsing(boolean b);
    boolean getUsing();

}
