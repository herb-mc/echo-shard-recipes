package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.api.PacketInterface;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin implements PacketInterface {

    @Shadow @Final private Identifier channel;
    @Shadow @Final private PacketByteBuf data;

    @Override
    public Identifier getChannel() {
        return channel;
    }

    @Override
    public PacketByteBuf getData() {
        return data;
    }

}
