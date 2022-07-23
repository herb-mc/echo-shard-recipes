package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.AttributeHelper;
import com.herb_mc.echo_shard_recipes.helper.Network;
import com.herb_mc.echo_shard_recipes.helper.ParticleHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.ATTRIBUTE;
import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.getAttribute;
import static com.herb_mc.echo_shard_recipes.helper.ProjectileHelper.spawnFrag;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Inject(
            method = "shoot",
            at = @At(
                    target = "Lnet/minecraft/item/CrossbowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;",
                    value = "INVOKE_ASSIGN"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void onShoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci, boolean bl, ProjectileEntity projectileEntity) {
        NbtCompound nbt = crossbow.getNbt();
        if (nbt != null) {
            if (nbt.getBoolean(ParticleHelper.HAS_PARTICLE)) {
                ((PersistentProjectileEntityInterface) projectileEntity).setParticle(nbt.getInt(ParticleHelper.PARTICLE));
                projectileEntity.getDataTracker().set(Network.PARTICLE, nbt.getInt(ParticleHelper.PARTICLE));
            }
            if (nbt.getBoolean(AttributeHelper.HAS_ATTRIBUTE)) {
                ((PersistentProjectileEntityInterface) projectileEntity).setAttribute(nbt.getString(ATTRIBUTE));
                switch (nbt.getString(ATTRIBUTE)) {
                    case "metaphysical" -> ((PersistentProjectileEntity) projectileEntity).setNoClip(true);
                    default -> {}
                }
            }
        }
        for (ItemStack item : shooter.getArmorItems()) switch(getAttribute(item)) {
            case "snipe_shot" -> ((PersistentProjectileEntityInterface) projectileEntity).addDamageMultiplier(0.1f);
            case "infernal" -> ((PersistentProjectileEntityInterface) projectileEntity).addFlatDamage(2);
            default -> {}
        }
    }

    @Inject(
            method = "shoot",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/ProjectileEntity;setVelocity(DDDFF)V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void removeRandomness(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci, boolean bl, ProjectileEntity projectileEntity, Vec3d vec3d, Quaternion quaternion, Vec3d vec3d2, Vec3f vec3f) {
        for (ItemStack item : shooter.getArmorItems()) if (getAttribute(item).equals("sharpshooter")) projectileEntity.setVelocity(vec3f.getX(), vec3f.getY(), vec3f.getZ(), speed, 0.4f);
    }

    @Inject(
            method = "use",
            at = @At(
                    target = "Lnet/minecraft/item/CrossbowItem;shootAll(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;FF)V",
                    value = "INVOKE"
            )
    )
    private void fragShot(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        NbtCompound nbt = user.getStackInHand(hand).getNbt();
        if (nbt != null && "buckshot".equals(nbt.getString(ATTRIBUTE))) {
            float speed = 1.0f;
            float divergence = 10.0f;
            for (ItemStack item : user.getArmorItems()) if (getAttribute(item).equals("sharpshooter")) divergence /= 1.25f;
            if (user.isSneaking()) {
                divergence /= 1.6f;
                speed *= 1.6f;
            }
            float bonus = EnchantmentHelper.getLevel(Enchantments.PIERCING, user.getStackInHand(hand)) / 8.0f;
            for (int i = 0; i < 8; i++) spawnFrag(world, user, bonus, speed, divergence);
        }
    }

}
