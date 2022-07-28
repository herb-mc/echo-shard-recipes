package com.herb_mc.echo_shard_recipes.mixin.server;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.Network.ECHO_C2S_ON_JOIN;
import static com.herb_mc.echo_shard_recipes.helper.Network.CHANNEL;
import static com.herb_mc.echo_shard_recipes.helper.Network.EXEMPT_PLAYERS;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "onCustomPayload",
            at = @At("HEAD"),
            cancellable = true
    )
    private void syncPlayer(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (CHANNEL.equals(packet.getChannel())) {
            PacketByteBuf data = packet.getData();
            if (data != null && data.readVarInt() == ECHO_C2S_ON_JOIN) {
                EXEMPT_PLAYERS.add(player);
                EchoShardRecipesMod.LOGGER.info("Player {} joined with Echo Shard Mod", player.getName().getString());
            }
            ci.cancel();
        }
    }

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void removeExemptPlayer(Text reason, CallbackInfo ci) {
        EXEMPT_PLAYERS.remove(player);
    }

}
