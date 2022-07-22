package com.herb_mc.echo_shard_recipes.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.getAttribute;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(
            method = "copyFrom",
            at = @At("HEAD")
    )
    protected void copySoulbound(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        ServerPlayerEntity thisEntity = (ServerPlayerEntity) (Object) this;
        if (!alive) {
            PlayerInventory inventory = oldPlayer.getInventory();
            for (int slot = 0; slot < inventory.size(); slot++)
                if ("soulbound".equals(getAttribute(inventory.getStack(slot))))
                    switch (slot) {
                        case 36 -> thisEntity.getInventory().armor.set(0, inventory.getStack(36));
                        case 37 -> thisEntity.getInventory().armor.set(1, inventory.getStack(37));
                        case 38 -> thisEntity.getInventory().armor.set(2, inventory.getStack(38));
                        case 39 -> thisEntity.getInventory().armor.set(3, inventory.getStack(39));
                        case 40 -> thisEntity.getInventory().offHand.set(0, inventory.getStack(40));
                        default -> thisEntity.getInventory().insertStack(inventory.getStack(slot));
                    }
        }
    }

}
