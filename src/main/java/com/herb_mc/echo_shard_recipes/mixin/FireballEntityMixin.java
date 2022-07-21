package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.ExplosiveProjectileEntityInterface;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FireballEntity.class)
public class FireballEntityMixin {

    @ModifyArg(
            method = "onCollision",
            at = @At(
                    target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZLnet/minecraft/world/explosion/Explosion$DestructionType;)Lnet/minecraft/world/explosion/Explosion;",
                    value = "INVOKE"
            ),
            index = 6
    )
    private Explosion.DestructionType preventBlockDamage(Explosion.DestructionType destructionType) {
        return (((ExplosiveProjectileEntityInterface) this).getLimit()) ? Explosion.DestructionType.NONE : destructionType;
    }

}
