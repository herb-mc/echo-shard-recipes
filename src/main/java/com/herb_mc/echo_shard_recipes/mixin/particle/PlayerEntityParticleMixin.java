package com.herb_mc.echo_shard_recipes.mixin.particle;

import com.herb_mc.echo_shard_recipes.api.LivingEntityInterface;
import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.ParticleHelper;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityParticleMixin {

    @Unique
    private static TrackedData<Integer> PARTICLE;

    @Inject(
            method = "<clinit>",
            at = @At("TAIL")
    )
    private static void initParticles(CallbackInfo ci) {
        PARTICLE = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    @Inject(
            method = "initDataTracker",
            at = @At("RETURN")
    )
    private void initParticleTracker(CallbackInfo ci) {
        ((PlayerEntity) (Object) this).getDataTracker().startTracking(PARTICLE, -1);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void updateParticle(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        int particle = -1;
        if (!player.world.isClient()) for (ItemStack i: player.getArmorItems()) {
            NbtCompound nbt = i.getNbt();
            if (nbt != null && nbt.getBoolean(ParticleHelper.HAS_PARTICLE)) particle = nbt.getInt(ParticleHelper.PARTICLE);
            player.getDataTracker().set(PARTICLE, particle);
        }
        ((LivingEntityInterface) player).setParticle(player.getDataTracker().get(PARTICLE));
    }

}
