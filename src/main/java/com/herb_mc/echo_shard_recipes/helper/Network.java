package com.herb_mc.echo_shard_recipes.helper;

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
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class Network {

    public static final Identifier CHANNEL = new Identifier("echo_shard_recipes:register");
    public static final int ECHO_C2S_ON_JOIN = 2;
    public static final Set<ServerPlayerEntity> EXEMPT_PLAYERS = new HashSet<>();

    public static void spawnParticles(World world, ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed, boolean forceSend) {
        ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(particle, true, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
        for (int j = 0; j < world.getPlayers().size(); ++j) {
            if (world instanceof ServerWorld && (!EXEMPT_PLAYERS.contains(((ServerWorld) world).getPlayers().get(j)) || forceSend))
                sendToPlayerWithinRenderDistance((ServerWorld) world, ((ServerWorld) world).getPlayers().get(j), x, y, z, particleS2CPacket, forceSend);
        }
        if (world.isClient() && !forceSend) {
            for (int j = 0; j < count; j++) {
                double dx = world.random.nextGaussian() * speed;
                double dy = world.random.nextGaussian() * speed;
                double dz = world.random.nextGaussian() * speed;
                world.addParticle(particle, true, x + world.random.nextGaussian() * deltaX, y + world.random.nextGaussian() * deltaY, z + world.random.nextGaussian() * deltaZ, dx, dy, dz);
            }
        }
    }

    public static void playSound(ServerWorld world, LivingEntity entity, SoundEvent s, float volume, float pitch) {
        world.playSoundFromEntity( null, entity, s, SoundCategory.MASTER, volume, pitch, 102);
    }

    private static void sendToPlayerWithinRenderDistance(ServerWorld world, ServerPlayerEntity player, double x, double y, double z, Packet<?> packet, boolean forceSend) {
        if (!(player.getWorld() != world || (EXEMPT_PLAYERS.contains(player) && !forceSend))) {
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
