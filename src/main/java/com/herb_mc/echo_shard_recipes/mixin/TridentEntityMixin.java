package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.PersistentProjectileEntityInterface;
import net.minecraft.entity.projectile.TridentEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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
