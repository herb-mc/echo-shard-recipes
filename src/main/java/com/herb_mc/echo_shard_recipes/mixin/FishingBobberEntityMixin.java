package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.helper.FishingBobberEntityInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin implements FishingBobberEntityInterface {

    @Unique int attribute = 0;

    @Override
    public void setAttribute(int i) {
        attribute = i;
    }

    @Inject(
            method = "onEntityHit",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
                    value = "INVOKE",
                shift = At.Shift.AFTER
            ),
            cancellable = true)
    public void shouldDamage(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (attribute == 1 && entityHitResult.getEntity() instanceof LivingEntity) {
            FishingBobberEntity f = (FishingBobberEntity) (Object) this;
            entityHitResult.getEntity().damage(DamageSource.thrownProjectile(f, f.getPlayerOwner()), 1.0f);
            f.discard();
            ci.cancel();
        }
    }

    @Inject(
            method = "removeIfInvalid",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;discard()V",
                    value = "INVOKE"
            ),
            cancellable = true
    )
    public void highTestLongerLine(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (attribute == 3 && ((FishingBobberEntity) (Object) this).squaredDistanceTo(player) < 4096) cir.setReturnValue(false);
    }

    @ModifyArg(
            method = "pullHookedEntity",
            at = @At(
                    target = "Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private Vec3d pullModifier(Vec3d velocity) {
        return attribute == 2 ? velocity.multiply(3.0) : velocity;
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("attribute", attribute);
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("attribute")) attribute = nbt.getInt("attribute");
    }

}
