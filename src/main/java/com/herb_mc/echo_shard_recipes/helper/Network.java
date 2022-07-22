package com.herb_mc.echo_shard_recipes.helper;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldEventS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class Network {

    public static void sendToPlayerInRange(ServerWorld world, ServerPlayerEntity player, double x, double y, double z, Packet<?> packet, double range) {
        if (player.getWorld() != world) return;
        else {
            BlockPos blockPos = player.getBlockPos();
            if (blockPos.isWithinDistance(new Vec3d(x, y, z), range))
                player.networkHandler.sendPacket(packet);
        }
    }

    public static void spawnParticles(ServerWorld world, ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(particle, true, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
        for(int j = 0; j < world.getPlayers().size(); ++j)
            sendToPlayerWithinRenderDistance(world, world.getPlayers().get(j), x, y, z, particleS2CPacket);
    }

    public static void playSound(ServerWorld world, LivingEntity entity, SoundEvent s, float volume, float pitch) {
        world.playSoundFromEntity( null, entity, s, SoundCategory.MASTER, volume, pitch, 102);
    }

    private static void sendToPlayerWithinRenderDistance(ServerWorld world, ServerPlayerEntity player, double x, double y, double z, Packet<?> packet) {
        if (player.getWorld() != world) return;
        else {
            BlockPos blockPos = player.getBlockPos();
            if (blockPos.isWithinDistance(new Vec3d(x, y, z), 128.0D))
                player.networkHandler.sendPacket(packet);
        }
    }

    public static void breakBlock(World world, BlockPos pos, boolean drop, @Nullable Entity breakingEntity, int maxUpdateDepth, @Nullable ItemStack it) {
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.isAir()) {
            FluidState fluidState = world.getFluidState(pos);
            if (!(blockState.getBlock() instanceof AbstractFireBlock)) world.syncWorldEvent(2001, pos, Block.getRawIdFromState(blockState));
            if (drop) {
                BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(pos) : null;
                Block.dropStacks(blockState, world, pos, blockEntity, breakingEntity, it == null ? ItemStack.EMPTY : it);
            }
            boolean bl = world.setBlockState(pos, fluidState.getBlockState(), 3, maxUpdateDepth);
            if (bl) world.emitGameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Emitter.of(breakingEntity, blockState));
        }
    }

    public static void emitBlockBreakParticles(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.isAir() && !(blockState.getBlock() instanceof AbstractFireBlock)) world.syncWorldEvent(2001, pos, Block.getRawIdFromState(blockState));
    }

}
