package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.AttributeHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;
import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.getAttribute;

@Mixin(Block.class)
public abstract class BlockMixin {

    @Inject(
            method = "afterBreak",
            at = @At(
                    target = "Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V",
                    value = "INVOKE"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    public void terraformingNoDrops(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack, CallbackInfo ci) {
        if("terraforming".equals(getAttribute(player.getMainHandStack()))) ci.cancel();
    }

    @Inject(
            method = "afterBreak",
            at = @At("HEAD"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void afterBreakHandler(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack, CallbackInfo ci) {
        NbtCompound nbt = player.getMainHandStack().getNbt();
        if (nbt != null) switch (nbt.getString(AttributeHelper.ATTRIBUTE)) {
            case "attuned" -> {if (ECHO_SHARD_RANDOM.nextFloat() <= 0.05f) player.addExperience(1);}
            default -> {}
        }
    }

}
