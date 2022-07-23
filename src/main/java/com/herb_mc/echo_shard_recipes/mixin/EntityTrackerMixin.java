package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public class EntityTrackerMixin {

    @Shadow @Final Entity entity;

    @ModifyArg(
            method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V",
            at = @At(
                    target = "Ljava/lang/Math;min(II)I",
                    value = "INVOKE"
            ),
            index = 0
    )
    public int trackedDistance(int a) {
        if (entity instanceof PersistentProjectileEntity && ((PersistentProjectileEntityInterface) entity).getParticle() >= 0)
            if (((PersistentProjectileEntity) entity).isCritical() || (entity instanceof TridentEntity && !((TridentEntityAccessor) entity).getDealtDamage())) a = 128;
        return a;
    }

}

