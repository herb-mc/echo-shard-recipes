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
    public void setFireproof(CallbackInfoReturnable<Boolean> info) {
        ItemEntity thisEntity = (ItemEntity) (Object) this;
        if ("fireproof".equals(HelperMethods.getAttribute(thisEntity.getStack()))) info.setReturnValue(true);
    }

}
