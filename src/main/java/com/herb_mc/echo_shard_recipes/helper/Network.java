package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Network {

    private static void sendToPlayerWithinRenderDistance(ServerWorld world, ServerPlayerEntity player, double x, double y, double z, Packet<?> packet) {
        if (player.getWorld() != world) return;
        else {
            BlockPos blockPos = player.getBlockPos();
            if (blockPos.isWithinDistance(new Vec3d(x, y, z), 128.0D))
                player.networkHandler.sendPacket(packet);
        }
    }

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
        PlaySoundFromEntityS2CPacket packet = new PlaySoundFromEntityS2CPacket(s, SoundCategory.MASTER, entity, volume, pitch, 102);
        for(int j = 0; j < world.getPlayers().size(); ++j)
            sendToPlayerInRange(world, world.getPlayers().get(j), entity.getX(), entity.getY(), entity.getZ(), packet, 40);
    }

}
