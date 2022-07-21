package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.ThrownItemEntityInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.ECHO_SHARD_RANDOM;

@Mixin(SnowballEntity.class)
public class SnowballEntityMixin {

    @Inject(
            method = "onEntityHit",
            at = @At(
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    value = "INVOKE"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void damageEvent(EntityHitResult entityHitResult, CallbackInfo ci, Entity entity, int i) {
        switch (((ThrownItemEntityInterface) this).getAttribute()) {
            case "buckshot" -> {
                float f = 0.0f;
                SnowballEntity s = (SnowballEntity) (Object) this;
                if (entity instanceof LivingEntity && !((LivingEntity) entity).isBlocking()) {
                    if ((entity instanceof ShulkerEntity && ((ShulkerEntityAccessor) entity).closed()) || (entity instanceof WitherEntity && ((WitherEntity) entity).shouldRenderOverlay())) f = ECHO_SHARD_RANDOM.nextFloat();
                    if (f < 0.3f) {
                        ((LivingEntity) entity).hurtTime = 0;
                        entity.timeUntilRegen = 1;
                        entity.damage(DamageSource.thrownProjectile(s, s.getOwner()), 0.5f + ((ThrownItemEntityInterface) this).getBonusDamage());
                        ci.cancel();
                    }
                } else if (entity instanceof EnderDragonPart && ECHO_SHARD_RANDOM.nextFloat() < 0.3f) {
                    ((EnderDragonPart) entity).owner.hurtTime = 0;
                    ((EnderDragonPart) entity).owner.timeUntilRegen = 1;
                    ((EnderDragonPart) entity).owner.damagePart((EnderDragonPart) entity, DamageSource.thrownProjectile(s, s.getOwner()), 0.5f + ((ThrownItemEntityInterface) this).getBonusDamage());
                    ci.cancel();
                }
                else if (!(entity instanceof LivingEntity)) {
                    entity.timeUntilRegen = 1;
                    entity.damage(DamageSource.thrownProjectile(s, s.getOwner()), 0.5f + ((ThrownItemEntityInterface) this).getBonusDamage());
                    ci.cancel();
                }
            }
            case "flamethrower" -> {
                SnowballEntity s = (SnowballEntity) (Object) this;
                if (entity instanceof LivingEntity && !((LivingEntity) entity).isBlocking() && !entity.isFireImmune()) {
                    ((LivingEntity) entity).hurtTime = 0;
                    entity.timeUntilRegen = 1;
                    entity.damage(DamageSource.thrownProjectile(s, s.getOwner()), 1.0f);
                    entity.setFireTicks(30);
                }
                ci.cancel();
            }
            case "gun_ho" -> {
                SnowballEntity s = (SnowballEntity) (Object) this;
                float d = ((ThrownItemEntityInterface) s).getDamage();
                if (entity instanceof LivingEntity && !((LivingEntity) entity).isBlocking()) {
                    ((LivingEntity) entity).hurtTime = 0;
                    entity.timeUntilRegen = 1;
                    entity.damage(DamageSource.thrownProjectile(s, s.getOwner()), d);
                } else if (entity instanceof EnderDragonPart) {
                    ((EnderDragonPart) entity).owner.hurtTime = 0;
                    ((EnderDragonPart) entity).owner.timeUntilRegen = 1;
                    ((EnderDragonPart) entity).owner.damagePart((EnderDragonPart) entity, DamageSource.thrownProjectile(s, s.getOwner()), d);
                } else if (!(entity instanceof LivingEntity)) {
                    entity.timeUntilRegen = 1;
                    entity.damage(DamageSource.thrownProjectile(s, s.getOwner()), d);
                }
                ci.cancel();
            }
        }
    }

    @Inject(
            method = "onCollision",
            at = @At("HEAD"),
            cancellable = true
    )
    private void rocket(HitResult hitResult, CallbackInfo ci) {
        if ("rocket".equals(((ThrownItemEntityInterface) this).getAttribute())){
            SnowballEntity s = (SnowballEntity) (Object) this;
            s.world.createExplosion(s.getOwner(), s.getX(), s.getY(), s.getZ(), ((ThrownItemEntityInterface) s).getDamage(), Explosion.DestructionType.NONE);
            s.discard();
            ci.cancel();
        }
    }

}
