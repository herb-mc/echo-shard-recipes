package com.herb_mc.echo_shard_recipes.mixin.init;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class BootstrapMixin {

    @Inject(
            method = "initialize",
            at = @At(
                    target = "Lnet/minecraft/recipe/BrewingRecipeRegistry;registerDefaults()V",
                    value = "INVOKE"
            )
    )
    private static void initializeRecipes(CallbackInfo ci) {
        EchoShardRecipesMod.init();
    }

}
