package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.AttributeHelper;
import com.herb_mc.echo_shard_recipes.helper.Network;
import com.herb_mc.echo_shard_recipes.helper.ParticleHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.ATTRIBUTE;

@Mixin(TridentItem.class)
public class TridentItemMixin {

    @Inject(
            method = "onStoppedUsing",
            at = @At(
                    target = "Lnet/minecraft/entity/projectile/TridentEntity;setVelocity(Lnet/minecraft/entity/Entity;FFFFF)V",
                    value = "INVOKE"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void addParticles(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci, PlayerEntity playerEntity, int i, int j, TridentEntity tridentEntity) {
        if (!world.isClient()) {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null) {
                if (nbt.getBoolean(ParticleHelper.HAS_PARTICLE))
                    ((PersistentProjectileEntityInterface) tridentEntity).setParticle(nbt.getInt(ParticleHelper.PARTICLE));
                if (nbt.getBoolean(AttributeHelper.HAS_ATTRIBUTE))
                    ((PersistentProjectileEntityInterface) tridentEntity).setAttribute(nbt.getString(ATTRIBUTE));
            }
        }
    }

}
