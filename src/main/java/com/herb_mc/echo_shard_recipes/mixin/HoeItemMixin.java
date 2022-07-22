package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.ItemStackInterface;
import com.herb_mc.echo_shard_recipes.helper.AttributeHelper;
import com.herb_mc.echo_shard_recipes.helper.ProjectileHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public class HoeItemMixin {

    @Inject(
            method = "useOnBlock",
            at = @At("HEAD"),
            cancellable = true
    )
    private void attributeEvents(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        PlayerEntity user = context.getPlayer();
        Hand hand = context.getHand();
        if (user != null && !user.world.isClient()) {
            ItemStack itemStack = user.getStackInHand(hand);
            switch(AttributeHelper.getAttribute(itemStack)) {
                case "gun_ho" -> {
                    if (!((ItemStackInterface) (Object) itemStack).hasCooldown() && ProjectileHelper.gunShoot(itemStack, user))
                        cir.setReturnValue(ActionResult.PASS);
                    else cir.setReturnValue(ActionResult.FAIL);}
            }
        }
    }

}