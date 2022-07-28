package com.herb_mc.echo_shard_recipes.mixin.client;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.Network.*;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow @Final private MinecraftClient client;

    @Inject(
            method = "onGameJoin",
            at = @At("RETURN")
    )
    private void onGameJoined(GameJoinS2CPacket packet, CallbackInfo info) {
        if (client.player != null) client.player.networkHandler.sendPacket(new CustomPayloadC2SPacket(CHANNEL, (new PacketByteBuf(Unpooled.buffer())).writeVarInt(ECHO_C2S_ON_JOIN)));
    }

}
