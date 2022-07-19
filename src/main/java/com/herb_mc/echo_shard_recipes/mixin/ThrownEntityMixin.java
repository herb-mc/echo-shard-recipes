package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.ThrownItemEntityInterface;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEntity.class)
public class ThrownEntityMixin {

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void removeIfInWater(CallbackInfo ci) {
        ThrownEntity e = (ThrownEntity) (Object) this;
        if (e instanceof ThrownItemEntity && "flamethrower".equals(((ThrownItemEntityInterface) e).getAttribute()) && e.isTouchingWater()) e.discard();
    }

}
