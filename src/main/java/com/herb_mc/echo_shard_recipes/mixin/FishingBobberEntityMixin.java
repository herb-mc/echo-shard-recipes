package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.FishingBobberEntityInterface;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntity.class)
public class FishingBobberEntityMixin implements FishingBobberEntityInterface {

    @Unique private int attribute = 0;
    @Unique private boolean shouldUpdate;

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
        boolean bl = player.getMainHandStack().isOf(Items.FISHING_ROD) || player.getOffHandStack().isOf(Items.FISHING_ROD);
        if (bl && attribute == 3 && ((FishingBobberEntity) (Object) this).squaredDistanceTo(player) < 4096) cir.setReturnValue(false);
    }

    @ModifyArg(
            method = "pullHookedEntity",
            at = @At(
                    target = "Lnet/minecraft/util/math/Vec3d;add(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
                    value = "INVOKE"
            )
    )
    private Vec3d pullModifier(Vec3d velocity) {
        shouldUpdate = attribute == 2;
        double scale = 5 / velocity.multiply(3.0).length();
        return attribute == 2 ? scale < 1 ? velocity.multiply(3.0 * scale) : velocity.multiply(3.0) : velocity;
    }

    @Inject(
            method = "pullHookedEntity",
            at = @At(
                    target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            )
    )
    private void updatePlayer(Entity entity, CallbackInfo ci) {
        if (shouldUpdate && entity instanceof ServerPlayerEntity && !entity.world.isClient()) ((ServerPlayerEntity) entity).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(entity));
    }

    @Inject(
            method = "tick",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V",
                    value = "INVOKE",
                    ordinal = 3,
                    shift = At.Shift.AFTER
            )
    )
    private void bobberLatch(CallbackInfo ci) {
        FishingBobberEntity f = ((FishingBobberEntity) (Object) this);
        if (!f.world.isClient() && attribute == 4) {
            f.setNoGravity(false);
            Box box = f.getBoundingBox().expand(0.02);
            double[] pos = {box.minX, box.maxX, box.minY, box.maxY, box.minZ, box.maxZ};
            for (int i = 0; i < 2; i++) for (int j = 2; j < 4; j++) for (int k = 4; k < 6; k++) {
                        BlockPos p = new BlockPos(pos[i], pos[j], pos[k]);
                        BlockState b = f.world.getBlockState(p);
                        VoxelShape vs = b.getCollisionShape(f.world, p, ShapeContext.of(f));
                        if (!vs.isEmpty() && vs.getBoundingBox().offset(p).intersects(box)) {
                            f.setNoGravity(true);
                            break;
                        }
            }
            if (f.hasNoGravity()) f.setVelocity(0, 0.03, 0);
            for (PlayerEntity p : f.world.getPlayers()) if (f.getPos().squaredDistanceTo(p.getPos()) < 16384) if (p instanceof ServerPlayerEntity && !p.world.isClient())
                ((ServerPlayerEntity) p).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(f));
            if (f.hasNoGravity()) f.setVelocity(0, 0, 0);
        }
    }

    @Inject(
            method = "use",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;discard()V",
                    value = "INVOKE"
            )
    )
    private void bobberGrapple(ItemStack usedItem, CallbackInfoReturnable<Integer> cir) {
        FishingBobberEntity f = (FishingBobberEntity) (Object) this;
        PlayerEntity p = f.getPlayerOwner();
        if (attribute == 4 && f.hasNoGravity() && p != null && !p.world.isClient()) {
            p.getItemCooldownManager().set(usedItem.getItem(), 30);
            if (!p.isCreative()) usedItem.damage(1, p.getRandom(), (ServerPlayerEntity) p);
            Vec3d v = f.getPos().subtract(p.getPos()).multiply(0.5);
            double scale = 2.0 / v.length();
            if (v.length() > 2.0) v = v.multiply(scale);
            p.addVelocity(v.x, v.y, v.z);
            if (p.getVelocity().y > 0) p.fallDistance = 0;
            ((ServerPlayerEntity) p).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(p));
        }
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
