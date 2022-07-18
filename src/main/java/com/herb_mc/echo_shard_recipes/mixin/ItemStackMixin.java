package com.herb_mc.echo_shard_recipes.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getAttribute;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(
            method = "isDamageable",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isUnbreakable(CallbackInfoReturnable<Boolean> cir) {
        if ("unbreakable".equals(getAttribute((ItemStack) (Object) this))) cir.setReturnValue(false);
    }

}
