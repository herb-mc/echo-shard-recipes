package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.ItemStackInterface;
import com.herb_mc.echo_shard_recipes.helper.Attributes;
import com.herb_mc.echo_shard_recipes.helper.Projectiles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {

    @Inject(
            method = "useOnBlock",
            at = @At("HEAD")
    )
    private void attributeEvents(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity user = context.getPlayer();
        Hand hand = context.getHand();
        if (user != null && !user.world.isClient()) {
            ItemStack itemStack = user.getStackInHand(hand);
            if (!((ItemStackInterface) (Object) itemStack).hasCooldown())
                switch(Attributes.getAttribute(itemStack)) {
                    case "fireball" -> Projectiles.shootFireball(itemStack, user);
                    case "flamethrower" -> Projectiles.flamethrower(itemStack, user);
                }
        }
    }

}
