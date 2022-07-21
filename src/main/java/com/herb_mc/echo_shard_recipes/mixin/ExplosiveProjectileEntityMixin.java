package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.ExplosiveProjectileEntityInterface;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.GameRules;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExplosiveProjectileEntity.class)
public class ExplosiveProjectileEntityMixin implements ExplosiveProjectileEntityInterface {

    @Unique private int timeLimit = 40;
    @Unique private boolean shouldLimit = false;

    @Override
    public void limitLifetime(boolean bl) {
        shouldLimit = bl;
    }

    @Override
    public boolean getLimit() {
        return shouldLimit;
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void discardIfTimeUp(CallbackInfo ci) {
        ExplosiveProjectileEntity e = ((ExplosiveProjectileEntity) (Object) this);
        if (shouldLimit && timeLimit == 0 && e instanceof FireballEntity) {
            boolean bl = e.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
            e.world.createExplosion(null, e.getX(), e.getY(), e.getZ(), 1, bl, Explosion.DestructionType.NONE);
            e.discard();
        }
        if (timeLimit > 0) timeLimit--;
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void customTimeLimit(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("timeLimit", timeLimit);
        nbt.putBoolean("shouldLimit", shouldLimit);
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void readCustomTimeLimit(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("timeLimit")) timeLimit =  nbt.getInt("timeLimit");
        if (nbt.contains("shouldLimit")) shouldLimit =  nbt.getBoolean("shouldLimit");
    }

}
