package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.ArrayList;
import java.util.List;

public class ItemHelper {

    public static ItemStack hasItem(PlayerInventory inventory, Item item) {
        for (int i = 0; i < inventory.size(); i++) if (inventory.getStack(i).getItem() == item) return inventory.getStack(i);
        return null;
    }

    public static void decrement(ItemStack i) {
        i.setCount(i.getCount() - 1);
    }

    public static List<ItemEntity> getNearestItems(LivingEntity livingEntity, double dist) {
        List<ItemEntity> list = livingEntity.world.getEntitiesByClass(ItemEntity.class, livingEntity.getBoundingBox().expand(dist), EntityPredicates.VALID_ENTITY);
        List<ItemEntity> finalList = new ArrayList<>();
        double sqDist = dist * dist;
        if (!list.isEmpty()) for (ItemEntity entity : list) if (VMath.getSquareDist(entity.getPos(), livingEntity.getPos()) < sqDist) finalList.add(entity);
        return finalList;
    }

    public static void addCooldown(PlayerEntity user, Item item, int cooldown) {
        user.getItemCooldownManager().set(item, cooldown);
    }

}
