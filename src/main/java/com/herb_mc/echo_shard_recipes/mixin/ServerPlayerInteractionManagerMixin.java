package com.herb_mc.echo_shard_recipes.mixin;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.getAttribute;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;

    @Shadow public abstract void finishMining(BlockPos pos, int sequence, String reason);

    @Inject(
            method = "processBlockBreakingAction",
            at = @At(
                    target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V",
                    value = "INVOKE",
                    ordinal = 0
            )
    )
    private void creativeBreakNeighbors(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        breakNeighors(pos, false, false, false, sequence, "creative destroy");
        if (player.isSneaking()) breakNeighors(pos, false, false, true, sequence, "creative destroy");
    }

    @Inject(
            method = "processBlockBreakingAction",
            at = @At(
                    target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V",
                    value = "INVOKE",
                    ordinal = 1
            )
    )
    private void instaBreakNeighbors(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        breakNeighors(pos, true, true, false, sequence, "insta mine");
        if (player.isSneaking()) breakNeighors(pos, true, true, true, sequence, "insta mine");
    }

    @Inject(
            method = "processBlockBreakingAction",
            at = @At(
                    target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V",
                    value = "INVOKE",
                    ordinal = 2
            )
    )
    private void breakNeighborsNormal(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        breakNeighors(pos, true, true, false, sequence, "destroyed");
        if (player.isSneaking()) breakNeighors(pos, true, true, true, sequence, "destroyed");
    }

    private void breakNeighors(BlockPos pos, boolean drop, boolean checkValid, boolean diagonal, int sequence, String reason) {
        if ("excavator".equals(getAttribute(player.getMainHandStack()))) {
            Direction direction = player.getPitch() > 45 ? Direction.DOWN : player.getPitch() < -45 ? Direction.DOWN : player.getHorizontalFacing();
            BlockPos[] breakPos = {pos, pos, pos, pos};
            if (direction != null) switch (direction) {
                    case UP, DOWN -> {
                        breakPos[0] = pos.add(new Vec3i(1, 0, diagonal ? 1 : 0));
                        breakPos[1] = pos.add(new Vec3i(-1, 0, diagonal ? -1 : 0));
                        breakPos[2] = pos.add(new Vec3i(diagonal ? -1 : 0, 0, 1));
                        breakPos[3] = pos.add(new Vec3i(diagonal ? 1 : 0, 0, -1));
                    }
                    case EAST, WEST -> {
                        breakPos[0] = pos.add(new Vec3i(0, 1, diagonal ? 1 : 0));
                        breakPos[1] = pos.add(new Vec3i(0, -1, diagonal ? -1 : 0));
                        breakPos[2] = pos.add(new Vec3i(0, diagonal ? -1 : 0, 1));
                        breakPos[3] = pos.add(new Vec3i(0, diagonal ? 1 : 0, -1));
                    }
                    case NORTH, SOUTH -> {
                        breakPos[0] = pos.add(new Vec3i(diagonal ? -1 : 0, 1, 0));
                        breakPos[1] = pos.add(new Vec3i(diagonal ? 1 : 0, -1, 0));
                        breakPos[2] = pos.add(new Vec3i(1, diagonal ? 1 : 0, 0));
                        breakPos[3] = pos.add(new Vec3i(-1, diagonal ? -1 : 0, 0));
                    }
                    default -> {}
                }
            for (BlockPos p : breakPos) if (!checkValid || player.canHarvest(world.getBlockState(p))) {
                if (!player.isCreative()) player.getMainHandStack().damage(1, player.getRandom(), player);
                finishMining(p, sequence, reason);
            }
        }
    }

}