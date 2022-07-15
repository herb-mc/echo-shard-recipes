package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.helper.PersistentProjectileEntityInterface;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;
import static com.herb_mc.echo_shard_recipes.helper.ParticleMethods.spawnParticles;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin implements PersistentProjectileEntityInterface {

    @Shadow private double damage;
    private int particle = -1;
    private String attribute = null;
    private static final Random random = new Random();

    @Override
    public int getParticle() {
        return particle;
    }

    @Override
    public String getAttribute() {
        return attribute;
    }

    @Override
    public void setParticle(int i) {
        particle = i;
    }

    @Override
    public void setAttribute(String s) {
        attribute = s;
    }

    @Inject(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private void createParticles(CallbackInfo ci) {
        PersistentProjectileEntity ref = (PersistentProjectileEntity) (Object) this;
        if (particle >= 0 && (ref.isCritical() || (ref instanceof TridentEntity && !((TridentEntityAccessor) ref).getDealtDamage()))) {
            ParticleItem i = PARTICLE_ITEMS[particle];
            for (int c = 0; c < i.particleCount; c++) {
                float xOffset = i.offsetScale * (0.5f - random.nextFloat()) + i.hardOffsetX;
                float yOffset = i.offsetScale * (0.5f - random.nextFloat()) + i.hardOffsetY;
                float zOffset = i.offsetScale * (0.5f - random.nextFloat()) + i.hardOffsetZ;
                if (ref.world instanceof ServerWorld)
                    spawnParticles((ServerWorld) ref.world, i.particle, ref.getX() + xOffset, ref.getY() + yOffset, ref.getZ() + zOffset, 1, 0, 0, 0, 0.03);
            }
        }
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (particle != -1) nbt.putInt(PARTICLE, particle);
        if (attribute != null) nbt.putString(ATTRIBUTE, attribute);
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(PARTICLE)) particle = nbt.getInt(PARTICLE);
        if (nbt.contains(ATTRIBUTE)) attribute = nbt.getString(ATTRIBUTE);
    }

    @ModifyArg(
            method = "onEntityHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    protected float increaseDamage(float f) {
        if ("razor_tip".equals(attribute) && ((PersistentProjectileEntity) (Object) this).isCritical()) f += damage - 1;
        return f;
    }

}
