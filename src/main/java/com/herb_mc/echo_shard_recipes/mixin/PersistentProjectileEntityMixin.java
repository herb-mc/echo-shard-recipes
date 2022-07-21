package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.Attributes;
import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;
import static com.herb_mc.echo_shard_recipes.helper.Network.*;

@Mixin(PersistentProjectileEntity.class)
public class PersistentProjectileEntityMixin implements PersistentProjectileEntityInterface {

    @Shadow private double damage;
    @Shadow protected boolean inGround;
    @Unique private int particle = -1;
    @Unique private boolean stopNoClip = false;
    @Unique private int flatDamageBoost = 0;
    @Unique private float damageMultiplier = 1.0f;
    @Unique private int ticksActive = 0;
    @Unique private String attribute = null;
    @Unique private Entity hitResult;
    @Unique private static final Random random = new Random();

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

    @Override
    public void addFlatDamage(int i) {
        flatDamageBoost += i;
    }

    @Override
    public void addDamageMultiplier(float f) {
        damageMultiplier += f;
    }

    @Inject(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private void processTick(CallbackInfo ci) {
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
        if (ticksActive > 40 && attribute != null) switch (attribute) {
                case "superphysical" -> {
                    spawnParticles((ServerWorld) ref.world, ParticleTypes.REVERSE_PORTAL, ref.getX(), ref.getY(), ref.getZ(), 20, 0, 0, 0, 0.1);
                    ref.discard();
                }
                case "metaphysical" -> ref.setNoClip(false);
                default -> {}
            }
    }

    @ModifyArg(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/util/math/Vec3d;multiply(D)Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private double processAttributes(double d) {
        PersistentProjectileEntity ref = ((PersistentProjectileEntity) (Object) this);
        if (attribute != null) switch (attribute) {
                case "metaphysical" -> ticksActive += 8;
                case "superphysical" -> {if (!ref.isTouchingWater()) d = 1.0; ticksActive += 2;}
                case "aquadynamic" -> d = ref.isTouchingWater() ? 1.0 : d;
                default -> {}
            }
        return d;
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        if (particle != -1) nbt.putInt(PARTICLE, particle);
        if (attribute != null) nbt.putString(ATTRIBUTE, attribute);
        nbt.putInt("flatDamageBoost", flatDamageBoost);
        nbt.putInt("ticksActive", ticksActive);
        nbt.putFloat("damageMultiplier", damageMultiplier);
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(PARTICLE)) particle = nbt.getInt(PARTICLE);
        if (nbt.contains(ATTRIBUTE)) attribute = nbt.getString(ATTRIBUTE);
        if (nbt.contains("flatDamageBoost")) flatDamageBoost = nbt.getInt("flatDamageBoost");
        if (nbt.contains("ticksActive")) ticksActive = nbt.getInt("ticksActive");
        if (nbt.contains("damageMultiplier")) damageMultiplier = nbt.getFloat("damageMultiplier");
    }

    @ModifyArg(
            method = "onEntityHit",
            at = @At(
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE"
            )
    )
    protected float increaseDamage(float f) {
        if (attribute != null) switch (attribute) {
                case "jagged" -> f += ((PersistentProjectileEntity) (Object) this).isCritical() ? damage - 1 : 0;
                case "super_luck" -> f += ((PersistentProjectileEntity) (Object) this).isCritical() ? random.nextInt((int) (f / 2)) : 0;
                default -> {}
            }
        return (f + flatDamageBoost) * damageMultiplier;
    }

    @Inject(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;getEntityCollision(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/hit/EntityHitResult;",
                    value = "INVOKE_ASSIGN"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void shouldModify(CallbackInfo ci, boolean bl, Vec3d vec3d, BlockPos blockPos, BlockState blockState, Vec3d vec3d3, Vec3d vec3d2, HitResult hitResult, EntityHitResult entityHitResult) {
        if (entityHitResult != null && "metaphysical".equals(attribute)) stopNoClip = true;
    }

    @ModifyVariable(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;getEntityCollision(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/hit/EntityHitResult;",
                    value = "INVOKE_ASSIGN",
                    shift = At.Shift.AFTER
            ),
            index = 1
    )
    private boolean isMetaphysical(boolean value) {
        if (value && "metaphysical".equals(attribute) && ticksActive < 40 && stopNoClip) return false;
        return value;
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void superphysicalDeletion(CallbackInfo ci) {
        PersistentProjectileEntity ref = (PersistentProjectileEntity) (Object) this;
        if ("superphysical".equals(attribute) && inGround) {
            spawnParticles((ServerWorld) ref.world, ParticleTypes.REVERSE_PORTAL, ref.getX(), ref.getY(), ref.getZ(), 20, 0, 0, 0, 0.1);
            ref.discard();
        }
    }

    @Inject(
            method = "onEntityHit",
            at = @At("HEAD")
    )
    protected void getEntityHitResult(EntityHitResult entityHitResult, CallbackInfo info) {
        hitResult = entityHitResult.getEntity();
    }

    @ModifyArg(
            method = "onEntityHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
            )
    )
    private Vec3d setReflected(Vec3d vec) {
        if (hitResult instanceof LivingEntity && ((LivingEntity) hitResult).isBlocking() && ((LivingEntity) hitResult).getActiveItem() != null && "reflecting".equals(Attributes.getAttribute(((LivingEntity) hitResult).getActiveItem())))
            vec = hitResult.getRotationVector().normalize().multiply(vec.length() * 7);
        return vec;
    }

}
