package com.herb_mc.echo_shard_recipes;

import com.herb_mc.echo_shard_recipes.recipes.EchoAugmentRecipe;
import com.herb_mc.echo_shard_recipes.recipes.EchoShardBaseRecipe;
import com.herb_mc.echo_shard_recipes.recipes.EchoWandRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class EchoShardRecipesMod {

    public static final String MOD_ID = "echo_shard_recipes";
    public static final Logger LOGGER = LoggerFactory.getLogger("echo_shard_recipes");
    public static final SpecialRecipeSerializer<EchoShardBaseRecipe> ECHO_SHARD_BASE = new SpecialRecipeSerializer<>(EchoShardBaseRecipe::new);
    public static final SpecialRecipeSerializer<EchoAugmentRecipe> ECHO_SHARD_AUGMENT = new SpecialRecipeSerializer<>(EchoAugmentRecipe::new);
    public static final SpecialRecipeSerializer<EchoWandRecipe> ECHO_WAND = new SpecialRecipeSerializer<>(EchoWandRecipe::new);
    public static final Random ECHO_SHARD_RANDOM = new Random();

    public static void init() {
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "crafting_special_echo_augments"), ECHO_SHARD_AUGMENT);
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "crafting_special_echo_shard_particle_base"), ECHO_SHARD_BASE);
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "crafting_special_echo_wand"), ECHO_WAND);
    }

}
