package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getAttribute;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Unique ItemStack stack = ItemStack.EMPTY;

    @ModifyArg(
            method = "dropAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"
            )
    )
    private ItemStack soulboundNoRemoval(ItemStack stack) {
        this.stack = ItemStack.EMPTY;
        boolean isSoulbound = "soulbound".equals(getAttribute(stack));
        if (isSoulbound) this.stack = stack;
        return isSoulbound ? ItemStack.EMPTY : stack;
    }

    @ModifyArg(
            method = "dropAll",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;set(ILjava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object soulboundNoDrop(Object stack) {
        return this.stack;
    }

}
