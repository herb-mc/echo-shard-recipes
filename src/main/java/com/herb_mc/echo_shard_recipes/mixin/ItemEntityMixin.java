package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.HelperMethods;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(
            method = "isFireImmune",
            at = @At("RETURN"),
            cancellable = true
    )
    public void setFireproof(CallbackInfoReturnable<Boolean> cir) {
        if ("fireproof".equals(HelperMethods.getAttribute(((ItemEntity) (Object) this).getStack()))) cir.setReturnValue(true);
    }

}
