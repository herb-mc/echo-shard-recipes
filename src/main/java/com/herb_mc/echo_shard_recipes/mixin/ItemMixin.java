package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.ItemStackInterface;
import com.herb_mc.echo_shard_recipes.helper.LivingEntityInterface;
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

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.*;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(
            method = "use",
            at = @At("HEAD")
    )
    private void handleUseEvents(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!user.world.isClient() && !((LivingEntityInterface) user).getUsing()) {
            ItemStack itemStack = user.getStackInHand(hand);
            if (!((ItemStackInterface) (Object) itemStack).hasCooldown())
                switch(getAttribute(itemStack)) {
                    case "fireball" -> shootFireball(itemStack, user);
                    case "flamethrower" -> flamethrower(itemStack, user);
                    case "gun_ho" ->  shoot(itemStack, user);
                }
        }
    }

}
