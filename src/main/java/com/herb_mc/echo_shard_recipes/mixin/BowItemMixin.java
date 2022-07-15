package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.helper.PersistentProjectileEntityInterface;
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
    private void setParticle(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, boolean bl, ItemStack itemStack, int i, float f, boolean bl2, ArrowItem arrowItem, PersistentProjectileEntity persistentProjectileEntity) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null){
            if (nbt.getBoolean(HAS_PARTICLE)) ((PersistentProjectileEntityInterface) persistentProjectileEntity).setParticle(nbt.getInt(PARTICLE));
            if (nbt.getBoolean(HAS_ATTRIBUTE)) ((PersistentProjectileEntityInterface) persistentProjectileEntity).setAttribute(nbt.getString(ATTRIBUTE));
        }
    }

}
