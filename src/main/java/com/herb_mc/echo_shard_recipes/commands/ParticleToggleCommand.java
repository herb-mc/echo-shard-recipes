package com.herb_mc.echo_shard_recipes.commands;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.helper.Network;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ParticleToggleCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("toggle-particles").executes(context -> {
                    if (context.getSource().isExecutedByPlayer()) {
                        ServerPlayerEntity p = context.getSource().getPlayer();
                        if (Network.EXEMPT_PLAYERS.remove(p)) {
                            context.getSource().sendFeedback(Text.of("Toggled non-essential particles on"), false);
                        }
                        else {
                            Network.EXEMPT_PLAYERS.add(p);
                            context.getSource().sendFeedback(Text.of("Toggled non-essential particles off"), false);
                        }
                        return 1;
                    }
                    EchoShardRecipesMod.LOGGER.info("This command can only be executed by a player");
                    return 0;
                }
        ));
    }

}
