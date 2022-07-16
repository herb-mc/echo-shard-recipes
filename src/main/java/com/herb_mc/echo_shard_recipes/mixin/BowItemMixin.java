package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.PersistentProjectileEntityInterface;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;
import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getAttribute;
import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.spawnFrag;
import static net.minecraft.item.BowItem.getPullProgress;

@Mixin(BowItem.class)
public class BowItemMixin {

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
            if (nbt.getBoolean(HAS_PARTICLE)) ((PersistentProjectileEntityInterface) persistentProjectileEntity).setParticle(nbt.getInt(PARTICLE));
            if (nbt.getBoolean(HAS_ATTRIBUTE)) {
                ((PersistentProjectileEntityInterface) persistentProjectileEntity).setAttribute(nbt.getString(ATTRIBUTE));
                switch (nbt.getString(ATTRIBUTE)) {
                    case "buckshot" -> {
                        if (getPullProgress(i) >= 1.0f) {
                            float speed = 1.4f;
                            float divergence = 8.0f;
                            for (ItemStack item : user.getArmorItems()) if (getAttribute(item).equals("sharpshooter")) divergence /= 1.5f;
                            if (user.isSneaking()) {
                                divergence /= 1.6f;
                                speed *= 1.5f;
                            }
                            float bonus = EnchantmentHelper.getLevel(Enchantments.POWER, stack) / 10.0f;
                            for (int j = 0; j < 12; j++) spawnFrag(world, user, bonus, speed, divergence);
                        }
                    }
                    case "metaphysical" -> {if (getPullProgress(i) >= 1.0f) persistentProjectileEntity.setNoClip(true);}
                    case "superphysical" -> persistentProjectileEntity.setNoGravity(true);
                    default -> {}
                }
            }
        }
        for (ItemStack item : user.getArmorItems()) if (getAttribute(item).equals("snipe_shot")) ((PersistentProjectileEntityInterface) persistentProjectileEntity).addDamageMultiplier(0.2f);
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
        for (ItemStack item : user.getArmorItems()) if (getAttribute(item).equals("sharpshooter")) persistentProjectileEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, f * 3.0F, 0.4F);
    }

}
