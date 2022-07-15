package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.PersistentProjectileEntityInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;
import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.ATTRIBUTE;

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
    private static void addParticles(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci, boolean bl, ProjectileEntity projectileEntity) {
        NbtCompound nbt = crossbow.getNbt();
        if (nbt != null){
            if (nbt.getBoolean(HAS_PARTICLE)) ((PersistentProjectileEntityInterface) projectileEntity).setParticle(nbt.getInt(PARTICLE));
            if (nbt.getBoolean(HAS_ATTRIBUTE)) ((PersistentProjectileEntityInterface) projectileEntity).setAttribute(nbt.getString(ATTRIBUTE));
        }
    }

}
