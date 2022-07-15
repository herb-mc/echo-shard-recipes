package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ParticleMethods {

    public static void spawnParticles(ServerWorld world, ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(particle, true, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
        for(int j = 0; j < world.getPlayers().size(); ++j)
            sendToPlayerWithinRenderDistance(world, world.getPlayers().get(j), x, y, z, particleS2CPacket);
    }

    private static void sendToPlayerWithinRenderDistance(ServerWorld world, ServerPlayerEntity player, double x, double y, double z, Packet<?> packet) {
        if (player.getWorld() != world) return;
        else {
            BlockPos blockPos = player.getBlockPos();
            if (blockPos.isWithinDistance(new Vec3d(x, y, z), 128.0D))
                player.networkHandler.sendPacket(packet);
        }
    }

}
