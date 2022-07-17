package com.herb_mc.echo_shard_recipes.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.function.Supplier;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.ATTRIBUTE;
import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.ECHO_SHARD_RANDOM;

@Mixin(Block.class)
public class BlockMixin {

    @Unique private static boolean noGravity;

    @ModifyArgs(
            method = "afterBreak",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    public void handleEnchantsAfterBreak(Args args){
        PlayerEntity player = args.get(4);
        NbtCompound nbt = player.getMainHandStack().getNbt();
        noGravity = nbt != null && "antigravity".equals(nbt.getString(ATTRIBUTE));
    }

    @Inject(
            method = "dropStack(Lnet/minecraft/world/World;Ljava/util/function/Supplier;Lnet/minecraft/item/ItemStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/ItemEntity;setToDefaultPickupDelay()V"
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private static void setStackGravity(World world, Supplier<ItemEntity> itemEntitySupplier, ItemStack stack, CallbackInfo info, ItemEntity itemEntity) {
        if (noGravity) {
            itemEntity.setNoGravity(true);
            itemEntity.setVelocity(MathHelper.nextDouble(world.random, -0.0125D, 0.0125D), MathHelper.nextDouble(world.random, -0.0125D, 0.0125D), MathHelper.nextDouble(world.random, -0.0125D, 0.0125D));
        }
    }

}
