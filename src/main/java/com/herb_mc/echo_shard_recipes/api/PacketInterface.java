package com.herb_mc.echo_shard_recipes.api;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface PacketInterface {

    Identifier getChannel();
    PacketByteBuf getData();

}
