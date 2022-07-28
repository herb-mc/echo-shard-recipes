package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public class TridentEntityMixin {

    @ModifyArg(
            method = "onEntityHit",
            at = @At(
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE"
            )
    )
    protected float modifyDamage(float f) {
        if (((TridentEntity) (Object) this).world.isClient() && "jagged".equals(((PersistentProjectileEntityInterface) this).getAttribute())) f += 2;
        return f;
    }

}
