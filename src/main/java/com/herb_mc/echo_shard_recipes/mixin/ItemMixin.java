package com.herb_mc.echo_shard_recipes.mixin;

import com.herb_mc.echo_shard_recipes.helper.ExplosiveProjectileEntityInterface;
import com.herb_mc.echo_shard_recipes.helper.HelperMethods;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.herb_mc.echo_shard_recipes.helper.HelperMethods.*;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(
            method = "use",
            at = @At("HEAD")
    )
    private void shootFireball(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (!user.world.isClient()) {
            ItemStack itemStack = user.getStackInHand(hand);
            switch(getAttribute(itemStack)) {
                case "fireball" -> {
                    boolean c = user.isCreative();
                    Vec3d v = user.getRotationVector();
                    boolean bl = user.isSneaking();
                    int power = user.isSneaking() ? 2 : 1;
                    if (!c) itemStack.damage(bl ? 3 : 1, user.getRandom(), (ServerPlayerEntity) user);
                    FireballEntity f = new FireballEntity(user.world, user, v.x, v.y, v.z, power);
                    f.setPosition(user.getEyePos().add(v.multiply(0.4)));
                    f.setVelocity(v.multiply(2));
                    playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_BLAZE_SHOOT, 1.0f, 0);
                    ((ExplosiveProjectileEntityInterface) f).limitLifetime(true);
                    user.world.spawnEntity(f);
                    user.getItemCooldownManager().set(itemStack.getItem(), bl ? 60 : 20);
                }
                case "flamethrower" -> {
                    boolean c = user.isCreative();
                    if (!c) itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
                    boolean bl = user.isSneaking();
                    playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_BLAZE_SHOOT, 0.8f, 0);
                    for (int i = 0; i < (bl ? 6 : 4); i++) spewFire(user.world, user, bl ? 1.4f : 1.2f, bl ? 7.5f : 10.0f);
                    user.getItemCooldownManager().set(itemStack.getItem(), 4);
                }
            }
        }
    }

}
