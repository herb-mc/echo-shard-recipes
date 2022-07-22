package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.Network;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.List;

import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.getAttribute;
import static com.herb_mc.echo_shard_recipes.helper.Entities.getFacing;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow @Final protected ServerPlayerEntity player;
    @Shadow protected ServerWorld world;
    @Shadow private boolean mining;

    @Shadow @Final private static Logger LOGGER;
    @Unique private final List<BlockPos> updatePos = new ArrayList<>();
    @Unique private BlockPos lastPos;

    @Inject(
            method = "processBlockBreakingAction",
            at = @At(
                    target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V",
                    value = "INVOKE",
                    ordinal = 0
            )
    )
    private void creativeBreakNeighbors(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (!world.isClient()) {
            breakNeighors(pos, false, false, sequence, "creative destroy", false);
            breakNeighors(pos, false, true, sequence, "creative destroy", false);
        }
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
        if (!world.isClient()) {
            breakNeighors(pos, true, false, sequence, "insta mine", true);
            breakNeighors(pos, true, true, sequence, "insta mine", true);
        }
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
        if (!world.isClient()) {
            breakNeighors(pos, true, false, sequence, "destroyed", true);
            breakNeighors(pos, true, true, sequence, "destroyed", true);
        }
    }

    @Inject(
            method = "continueMining",
            at = @At(
                    target = "Lnet/minecraft/server/world/ServerWorld;setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V",
                    value = "INVOKE",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void updateNeighbors(BlockState state, BlockPos pos, int failedStartMiningTime, CallbackInfoReturnable<Float> cir, int i, float f, int j) {
        if (!world.isClient()) {
            if (!pos.equals(lastPos)) refreshBlockProgress(-1);
            lastPos = pos;
            updateNeighbors(pos, player, false, j);
            updateNeighbors(pos, player, true, j);
        }
    }

    @Inject(
            method = "update",
            at = @At("HEAD")
    )
    private void updateMinedBlocks(CallbackInfo ci) {
        if (!world.isClient() && !mining) refreshBlockProgress(-1);
    }

    private void refreshBlockProgress(int progress) {
        int id = -player.getUuid().hashCode();
        int offset = 0;
        for (BlockPos b : updatePos) {
            updatePlayers(id - offset, b, progress);
            offset++;
        }
        updatePos.removeIf((e)->(true));
    }

    private void breakNeighors(BlockPos pos, boolean checkValid, boolean diagonal, int sequence, String reason, boolean drop) {
        if (!world.isClient() && "excavator".equals(getAttribute(player.getMainHandStack()))) {
            Direction direction = getFacing(player);
            BlockPos[] breakPos = getNeighbors(pos, diagonal, direction);
            for (BlockPos p : breakPos) if (!checkValid || player.canHarvest(world.getBlockState(p))) {
                if (!player.isCreative()) player.getMainHandStack().damage(1, player.getRandom(), player);
                Network.breakBlock(world, p, drop, player, 512, player.getMainHandStack());
            }
        }
    }

    private void updateNeighbors(BlockPos pos, PlayerEntity player, boolean diagonal, int progress) {
        if ("excavator".equals(getAttribute(player.getMainHandStack()))) {
            progress = Math.min(progress - 1, 9);
            Direction direction = getFacing(player);
            BlockPos[] breakPos = getNeighbors(pos, diagonal, direction);
            int offset = diagonal ? 4 : 0;
            int id = -player.getUuid().hashCode() - offset;
            updatePlayers(id, breakPos[0], progress);
            updatePlayers(id - 1, breakPos[1], progress);
            updatePlayers(id - 2, breakPos[2], progress);
            updatePlayers(id - 3, breakPos[3], progress);
            updatePos.add(breakPos[0]);
            updatePos.add(breakPos[1]);
            updatePos.add(breakPos[2]);
            updatePos.add(breakPos[3]);
        }
    }

    private BlockPos[] getNeighbors(BlockPos pos, boolean diagonal, Direction direction) {
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
        return breakPos;
    }

    private void updatePlayers(int id, BlockPos pos, int progress) {
        for (ServerPlayerEntity serverPlayerEntity : world.getServer().getPlayerManager().getPlayerList()) {
            if (serverPlayerEntity != null && serverPlayerEntity.world == world) {
                double d = (double) pos.getX() - serverPlayerEntity.getX();
                double e = (double) pos.getY() - serverPlayerEntity.getY();
                double f = (double) pos.getZ() - serverPlayerEntity.getZ();
                if (d * d + e * e + f * f < 1024.0D) {
                    serverPlayerEntity.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(id, pos, progress));
                }
            }
        }
    }

}