package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.ItemStackInterface;
import com.herb_mc.echo_shard_recipes.api.LivingEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.AttributeHelper;
import com.herb_mc.echo_shard_recipes.helper.ProjectileHelper;
import com.herb_mc.echo_shard_recipes.helper.Spells;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true
    )
    private void handleUseEvents(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!user.world.isClient()) {
            ItemStack itemStack = user.getStackInHand(hand);
            switch(AttributeHelper.getAttribute(itemStack)) {
                case "fireball" -> {if (validUse(itemStack, user)) ProjectileHelper.shootFireball(itemStack, user);}
                case "flamethrower" -> {if (validUse(itemStack, user)) ProjectileHelper.flamethrower(itemStack, user);}
                case "gun_ho" -> {
                    if (validUse(itemStack, user) && ProjectileHelper.gunShoot(itemStack, user))
                        cir.setReturnValue(TypedActionResult.consume(itemStack));
                    else cir.setReturnValue(TypedActionResult.pass(itemStack));
                }
                case "spell" -> {
                    if (validUse(itemStack, user) && Spells.processSpell(itemStack, user))
                        cir.setReturnValue(TypedActionResult.success(itemStack));
                    else cir.setReturnValue(TypedActionResult.pass(itemStack));
                }
            }
        }
    }

    private boolean validUse(ItemStack itemStack, PlayerEntity user) {
        return !((ItemStackInterface) (Object) itemStack).hasCooldown() && !((LivingEntityInterface) user).getUsing();
    }

}
