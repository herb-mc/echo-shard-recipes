package com.herb_mc.echo_shard_recipes.mixin.server;

import com.herb_mc.echo_shard_recipes.api.ServersideRecipe;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

@Mixin(SynchronizeRecipesS2CPacket.class)
public class SynchronizeRecipesS2CPacketMixin {

    @Final @Shadow private List<Recipe<?>> recipes;

    @Inject(method = "<init>(Ljava/util/Collection;)V", at = @At("RETURN"))
    public void onCreated(Collection<Recipe<?>> recipes, CallbackInfo ci) {
        this.recipes.removeIf(recipe -> recipe instanceof ServersideRecipe);
    }

}
