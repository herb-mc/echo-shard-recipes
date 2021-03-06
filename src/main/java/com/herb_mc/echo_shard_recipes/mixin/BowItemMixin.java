package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.AttributeHelper;
import com.herb_mc.echo_shard_recipes.helper.Network;
import com.herb_mc.echo_shard_recipes.helper.ParticleHelper;
import com.herb_mc.echo_shard_recipes.helper.ProjectileHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.herb_mc.echo_shard_recipes.helper.ProjectileHelper.createProjectile;
import static net.minecraft.item.BowItem.getPullProgress;

@Mixin(BowItem.class)
public abstract class BowItemMixin {

    @Inject(
            method = "onStoppedUsing",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V",
                    value = "INVOKE"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void onShoot(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            if (nbt.getBoolean(ParticleHelper.HAS_PARTICLE)) ((PersistentProjectileEntityInterface) persistentProjectileEntity).setParticle(nbt.getInt(ParticleHelper.PARTICLE));
            if (nbt.getBoolean(AttributeHelper.HAS_ATTRIBUTE)) {
                ((PersistentProjectileEntityInterface) persistentProjectileEntity).setAttribute(nbt.getString(AttributeHelper.ATTRIBUTE));
                switch (nbt.getString(AttributeHelper.ATTRIBUTE)) {
                    case "buckshot" -> {
                        if (getPullProgress(i) >= 1.0f) {
                            float div = 1.0f;
                            for (ItemStack item : user.getArmorItems()) if (AttributeHelper.getAttribute(item).equals("sharpshooter")) div = 1.5f;
                            float bonus = EnchantmentHelper.getLevel(Enchantments.POWER, stack) / 10.0f;
                            for (int j = 0; j < 12; j++) createProjectile(world, user, false, "buckshot", user.isSneaking() ? 2.1f : 1.4f, user.isSneaking() ? 5f / div : 8f / div, 0, bonus, Items.IRON_NUGGET, 0, true);
                        }
                    }
                    case "metaphysical" -> {if (getPullProgress(i) >= 1.0f) persistentProjectileEntity.setNoClip(true);}
                    case "superphysical" -> persistentProjectileEntity.setNoGravity(true);
                    default -> {}
                }
            }
        }
        for (ItemStack item : user.getArmorItems()) switch(AttributeHelper.getAttribute(item)) {
            case "snipe_shot" -> ((PersistentProjectileEntityInterface) persistentProjectileEntity).addDamageMultiplier(0.2f);
            case "infernal" -> ((PersistentProjectileEntityInterface) persistentProjectileEntity).addFlatDamage(3);
            default -> {}
        }
    }

    @Inject(
            method = "onStoppedUsing",
            at = @At(
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void arrowRaycasting(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && "hitscan".equals(nbt.getString(AttributeHelper.ATTRIBUTE)) && getPullProgress(i) >= 1.0f)
            if (ProjectileHelper.arrowHitscan((ServerWorld) world, 100, persistentProjectileEntity, user)) persistentProjectileEntity.discard();

    }

    @Inject(
            method = "onStoppedUsing",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void removeRandomness(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
        for (ItemStack item : user.getArmorItems()) if (AttributeHelper.getAttribute(item).equals("sharpshooter")) persistentProjectileEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, f * 3.0F, 0.4F);
    }

}
