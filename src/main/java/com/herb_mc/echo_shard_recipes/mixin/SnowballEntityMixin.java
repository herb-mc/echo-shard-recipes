package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.helper.ThrownItemEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Random;

@Mixin(SnowballEntity.class)
public class SnowballEntityMixin {

    private static final Random snowballRandom = new Random();

    @Inject(
            method = "onEntityHit",
            at = @At(
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void fragDamage(EntityHitResult entityHitResult, CallbackInfo ci, Entity entity, int i) {
        float f = 0.0f;
        EchoShardRecipesMod.LOGGER.info("{}", f);
        if ("buckshot".equals(((ThrownItemEntityInterface) this).getAttribute())) {
            if (entity instanceof LivingEntity && !((LivingEntity) entity).isBlocking()) {
                if ((entity instanceof ShulkerEntity && ((ShulkerEntityAccessor) entity).closed()) || (entity instanceof WitherEntity && ((WitherEntity) entity).shouldRenderOverlay())) f = snowballRandom.nextFloat();
                if (f < 0.3f) {
                    ((LivingEntity) entity).hurtTime = 0;
                    entity.timeUntilRegen = 1;
                    entity.damage(new ProjectileDamageSource("arrow", (SnowballEntity) (Object) this, ((SnowballEntity) (Object) this).getOwner()), 0.5f + ((ThrownItemEntityInterface) this).getBonusDamage());
                    ci.cancel();
                }
            } else if (entity instanceof EnderDragonPart && snowballRandom.nextFloat() < 0.3f) {
                ((EnderDragonPart) entity).owner.hurtTime = 0;
                ((EnderDragonPart) entity).owner.timeUntilRegen = 1;
                ((EnderDragonPart) entity).owner.damagePart((EnderDragonPart) entity, new ProjectileDamageSource("arrow", (SnowballEntity) (Object) this, ((SnowballEntity) (Object) this).getOwner()), 0.5f + ((ThrownItemEntityInterface) this).getBonusDamage());
                ci.cancel();
            }
        }
    }

}
