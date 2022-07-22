package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.FishingBobberEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.getAttribute;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {

    @Unique private int attribute = 0;

    @Inject(
            method = "use",
            at = @At(
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                    value = "INVOKE"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void getAttributeFromRod(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir, ItemStack itemStack, int i, int j) {
        switch (getAttribute(itemStack)) {
            case "jagged" -> attribute = 1;
            case "faster_reel" -> attribute = 2;
            case "high_test" -> attribute = 3;
            case "grappling" -> attribute = 4;
            default -> attribute = 0;
        }
    }

    @ModifyArg(
            method = "use",
            at = @At(
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                    value = "INVOKE"
            )
    )
    private Entity applyAttributeToBobber(Entity bobber) {
        ((FishingBobberEntityInterface) bobber).setAttribute(attribute);
        switch(attribute) {
            case 3 -> bobber.setVelocity(bobber.getVelocity().multiply(2.5));
            case 4 -> bobber.setVelocity(bobber.getVelocity().multiply(1.5));
            default -> {}
        }
        return bobber;
    }

}
