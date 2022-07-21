package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.ItemStackInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getAttribute;

@Mixin(ItemStack.class)
public class ItemStackMixin implements ItemStackInterface {

    @Unique private int itemCooldown = 0;

    @Override
    public void setCooldown(int i) {
        itemCooldown = i;
    }

    @Override
    public boolean hasCooldown() {
        return itemCooldown > 0;
    }

    @Inject(
            method = "isDamageable",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isUnbreakable(CallbackInfoReturnable<Boolean> cir) {
        if ("unbreakable".equals(getAttribute((ItemStack) (Object) this))) cir.setReturnValue(false);
    }

    @Inject(
            method = "inventoryTick",
            at = @At("HEAD")
    )
    private void tickCooldown(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (entity instanceof PlayerEntity && itemCooldown > 0) itemCooldown--;
    }

}
