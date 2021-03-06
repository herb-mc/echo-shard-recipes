package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.ThrownItemEntityInterface;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.Network.spawnParticles;

@Mixin(ThrownEntity.class)
public class ThrownEntityMixin {

    @Unique private int timer = 0;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void tick(CallbackInfo ci) {
        ThrownEntity e = (ThrownEntity) (Object) this;
        if (e instanceof ThrownItemEntity && ("rocket".equals(((ThrownItemEntityInterface) e).getAttribute()) || "gun_ho".equals(((ThrownItemEntityInterface) e).getAttribute()))) {
            if ("rocket".equals(((ThrownItemEntityInterface) e).getAttribute()))
                spawnParticles(e.world, ParticleTypes.SMOKE, e.getX(), e.getY(), e.getZ(), 1, 0, 0, 0, 0.05, false);
            if (((ThrownItemEntityInterface) e).getBonusDamage() >= 10) {
                if ("rocket".equals(((ThrownItemEntityInterface) e).getAttribute())) e.world.createExplosion(e.getOwner(), e.getX(), e.getY(), e.getZ(), ((ThrownItemEntityInterface) e).getDamage(), Explosion.DestructionType.NONE);
                e.discard();
            }
            ((ThrownItemEntityInterface) e).addBonusDamage(((ThrownItemEntityInterface) e).getIncrement());
        }
        if (e instanceof ThrownItemEntity && "flamethrower".equals(((ThrownItemEntityInterface) e).getAttribute()) && e.isTouchingWater()) e.discard();
    }

    @ModifyArg(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/util/math/Vec3d;multiply(D)Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private double bulletNoDrag(double value) {
        ThrownEntity e = (ThrownEntity) (Object) this;
        if (e instanceof ThrownItemEntity && ("rocket".equals(((ThrownItemEntityInterface) e).getAttribute()) || "gun_ho".equals(((ThrownItemEntityInterface) e).getAttribute()))) value = e.isSubmergedInWater() ? 0.92 : 1.0;
        if (e instanceof ThrownItemEntity && "sunbeam".equals(((ThrownItemEntityInterface) e).getAttribute())) value = 1.0;
        return value;
    }
}
