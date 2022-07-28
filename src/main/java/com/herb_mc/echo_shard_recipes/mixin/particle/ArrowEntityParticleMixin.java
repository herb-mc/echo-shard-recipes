package com.herb_mc.echo_shard_recipes.mixin.particle;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.ArrowEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ArrowEntity.class, priority = 1)
public class ArrowEntityParticleMixin {

    @Unique private static TrackedData<Integer> PARTICLE;

    @Inject(
            method = "<clinit>",
            at = @At("TAIL")
    )
    private static void initParticles(CallbackInfo ci) {
        PARTICLE = DataTracker.registerData(ArrowEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    @Inject(
            method = "initDataTracker",
            at = @At("RETURN")
    )
    private void initParticleTracker(CallbackInfo ci) {
        ((ArrowEntity) (Object) this).getDataTracker().startTracking(PARTICLE, -1);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void updateParticle(CallbackInfo ci) {
        ArrowEntity proj = (ArrowEntity) (Object) this;
        if (!proj.world.isClient()) proj.getDataTracker().set(PARTICLE, ((PersistentProjectileEntityInterface) proj).getParticle());
        ((PersistentProjectileEntityInterface) proj).setParticle(proj.getDataTracker().get(PARTICLE));
    }

}
