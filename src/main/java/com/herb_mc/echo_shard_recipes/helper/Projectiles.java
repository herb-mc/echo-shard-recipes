package com.herb_mc.echo_shard_recipes.helper;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.Iterator;

public class Projectiles {

    public static void gunShoot(ItemStack itemStack, PlayerEntity user) {
        Item it = itemStack.getItem();
        boolean c = user.isCreative();
        boolean isSneak = user.isSneaking();
        if ((((PlayerEntityInterface) user).canShoot() && itemStack == ((PlayerEntityInterface) user).getLastStack()) || net.minecraft.item.Items.IRON_HOE.equals(it))
            if (net.minecraft.item.Items.NETHERITE_HOE.equals(it)) {
                ItemStack ammo = Items.hasItem(user.getInventory(), net.minecraft.item.Items.GOLD_NUGGET);
                if ((ammo != null || c) && itemStack == user.getMainHandStack()) {
                    if (!c) itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
                    ((ThrownItemEntityInterface) createBullet(user.world, user, 4.0f, 0.0174533, 5.0f)).setIncrement(0.6f);
                    Vec3d rot = user.getRotationVector().multiply(-0.1);
                    user.addVelocity(rot.x, rot.y, rot.z);
                    if (user instanceof ServerPlayerEntity && !user.world.isClient())
                        ((ServerPlayerEntity) user).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    Items.addCooldown(user, itemStack.getItem(), 12);
                    ((LivingEntityInterface) user).setUsing(true);
                    if (!c) Items.decrement(ammo);
                    ((LivingEntityInterface) user).setBurst(4, itemStack);
                }
            }
            else if (net.minecraft.item.Items.DIAMOND_HOE.equals(it)) {
                ItemStack ammo = Items.hasItem(user.getInventory(), net.minecraft.item.Items.GOLD_NUGGET);
                if ((ammo != null || c) && itemStack == user.getMainHandStack()) {
                    if (!c) itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
                    ((ThrownItemEntityInterface) createBullet(user.world, user, 4.0f, 0.0174533 * (isSneak ? 1 : 2.5), 6.0f)).setIncrement(0.5f);
                    Vec3d rot = user.getRotationVector().multiply(isSneak ? -0.05 : -0.1);
                    user.addVelocity(rot.x, rot.y, rot.z);
                    if (user instanceof ServerPlayerEntity && !user.world.isClient())
                        ((ServerPlayerEntity) user).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    Items.addCooldown(user, itemStack.getItem(), 3);
                    ((LivingEntityInterface) user).setUsing(true);
                    if (!c) Items.decrement(ammo);
                }
            }
            else if (!((LivingEntityInterface) user).getUsing() && net.minecraft.item.Items.IRON_HOE.equals(it)) {
                ItemStack ammo = Items.hasItem(user.getInventory(), net.minecraft.item.Items.GOLD_NUGGET);
                if (ammo != null || c) {
                    if (!c) itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ITEM_CROSSBOW_SHOOT, 0.8f, 0.4f);
                    ((ThrownItemEntityInterface) createBullet(user.world, user, 4.0f, 0.0174533 * (isSneak ? 1.5 : 3.5), 6.0f)).setIncrement(1.0f);
                    Vec3d rot = user.getRotationVector().multiply(isSneak ? -0.05 : -0.1);
                    user.addVelocity(rot.x, rot.y, rot.z);
                    if (user instanceof ServerPlayerEntity && !user.world.isClient())
                        ((ServerPlayerEntity) user).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    ((ItemStackInterface) (Object) itemStack).setCooldown(12);
                    ((LivingEntityInterface) user).setUsing(true);
                    if (!c) Items.decrement(ammo);
                }
            }
            else if (net.minecraft.item.Items.STONE_HOE.equals(it)) {
                ItemStack ammo = Items.hasItem(user.getInventory(), net.minecraft.item.Items.GOLD_INGOT);
                if ((ammo != null || c) && itemStack == user.getMainHandStack()) {
                    if (!c) itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
                    for (int i = 0; i < user.getRandom().nextBetween(12, 16); i++)
                        ((ThrownItemEntityInterface) createBullet(user.world, user, 3.0f, 0.0174533 * 5, 2.0f)).setIncrement(0.8f);
                    Vec3d rot = user.getRotationVector().multiply(-1);
                    user.addVelocity(rot.x, rot.y, rot.z);
                    if (user instanceof ServerPlayerEntity && !user.world.isClient())
                        ((ServerPlayerEntity) user).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    Items.addCooldown(user, itemStack.getItem(), 50);
                    ((LivingEntityInterface) user).setUsing(true);
                    if (!c) Items.decrement(ammo);
                }
            }
            else if (net.minecraft.item.Items.WOODEN_HOE.equals(it)) {
                ItemStack ammo = Items.hasItem(user.getInventory(), net.minecraft.item.Items.GOLD_INGOT);
                if ((ammo != null || c) && itemStack == user.getMainHandStack()) {
                    if (!c) itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ITEM_TOTEM_USE, 0.35f, 2);
                    Vec3d rot = user.getRotationVector().multiply(isSneak ? -0.1 : -0.3);
                    user.addVelocity(rot.x, rot.y, rot.z);
                    Vec3d dir = user.getRotationVector().normalize();
                    double deg = 0;
                    if (!user.isSneaking()) deg += 0.01;
                    if (!user.isOnGround()) deg += 0.01;
                    if (user.isOnFire()) deg += 0.03;
                    if (deg > 0) dir = VMath.applyDivergence(dir, deg);
                    dir = dir.multiply(0.2);
                    if (user instanceof ServerPlayerEntity && !user.world.isClient()) {
                        gunRaycast((ServerWorld) user.world, 500, user.getEyePos(), dir, user);
                        ((ServerPlayerEntity) user).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    }
                    Items.addCooldown(user, itemStack.getItem(), 60);
                    ((LivingEntityInterface) user).setUsing(true);
                    if (!c) Items.decrement(ammo);
                }
            }
            else if (net.minecraft.item.Items.GOLDEN_HOE.equals(it)) {
                ItemStack ammo = Items.hasItem(user.getInventory(), net.minecraft.item.Items.GUNPOWDER);
                if ((ammo != null || c) && itemStack == user.getMainHandStack()) {
                    if (!c) itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_TNT_PRIMED, 0.6f, 2);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
                    Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
                    ((ThrownItemEntityInterface) createRocket(user.world, user, 1.0f, 0.0174533 * 1.5, 4)).setIncrement(0.25f);
                    Vec3d rot = user.getRotationVector().multiply(-1.4);
                    user.addVelocity(rot.x, rot.y, rot.z);
                    if (user instanceof ServerPlayerEntity && !user.world.isClient())
                        ((ServerPlayerEntity) user).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
                    Items.addCooldown(user, itemStack.getItem(), 90);
                    ((LivingEntityInterface) user).setUsing(true);
                    if (!c) Items.decrement(ammo);
                }
            }
    }

    private static void gunRaycast(ServerWorld world, int limit, Vec3d pos, Vec3d dir, Entity user) {
        BlockPos b;
        VoxelShape vs;
        Box box;
        ParticleEffect p = new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.getDefaultState());
        Vec3d v = new Vec3d(0.15, 0.15, 0.15);
        for (int iter = 0; iter < limit; iter++) {
            b = new BlockPos(pos);
            vs = world.getBlockState(b).getCollisionShape(world, b, ShapeContext.of(user));
            if (!vs.isEmpty() && vs.getBoundingBox().offset(b).contains(pos)) return;
            if (iter % 3 == 0) Network.spawnParticles(world, p, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.05);
            box = new Box(pos.subtract(v), pos.add(v));
            for (Iterator<Entity> it = world.getOtherEntities(user, box, (e) -> (true)).iterator(); it.hasNext(); ) {
                Entity e = it.next();
                float damage = 22;
                double dist = Math.pow((e.getBoundingBox().maxX - e.getBoundingBox().minX) / 1.2, 2);
                if (pos.squaredDistanceTo(e.getEyePos()) < dist && user.isSneaking() && user.isOnGround()) damage *= 1.5f;
                Entities.damageEntity(e, damage, damage - 5, true, DamageSource.thrownProjectile(user, user));
                return;
            }
            pos = pos.add(dir);
        }
    }

    public static void spawnFrag(World world, LivingEntity user, float bonus, float speed, float divergence) {
        SnowballEntity frag = new SnowballEntity(world, user);
        ((ThrownItemEntityInterface) frag).setAttribute("buckshot");
        ((ThrownItemEntityInterface) frag).setBonusDamage(bonus);
        frag.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, speed, divergence);
        frag.setVelocity(user.getVelocity().multiply(0.1).add(frag.getVelocity()));
        frag.setItem(new ItemStack(net.minecraft.item.Items.IRON_NUGGET));
        world.spawnEntity(frag);
    }

    public static void spewFire(World world, LivingEntity user, float speed, float divergence) {
        SnowballEntity fireball = new SnowballEntity(world, user);
        ((ThrownItemEntityInterface) fireball).setAttribute("flamethrower");
        fireball.setFireTicks(20000);
        fireball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, speed, divergence);
        fireball.setVelocity(user.getVelocity().multiply(0.1).add(fireball.getVelocity()));
        fireball.setItem(new ItemStack(net.minecraft.item.Items.FIRE_CHARGE));
        world.spawnEntity(fireball);
    }

    public static SnowballEntity createBullet(World world, LivingEntity user, float speed, double divergence, float damage) {
        SnowballEntity bullet = new SnowballEntity(world, user);
        bullet.setNoGravity(true);
        ((ThrownItemEntityInterface) bullet).setAttribute("gun_ho");
        ((ThrownItemEntityInterface) bullet).setDamage(damage);
        bullet.setVelocity(VMath.applyDivergence(user.getRotationVector(), divergence).normalize().multiply(speed));
        bullet.setItem(new ItemStack(net.minecraft.item.Items.GOLD_NUGGET));
        world.spawnEntity(bullet);
        return bullet;
    }

    public static SnowballEntity createRocket(World world, LivingEntity user, float speed, double divergence, int explosionPower) {
        SnowballEntity bullet = new SnowballEntity(world, user);
        bullet.setNoGravity(true);
        ((ThrownItemEntityInterface) bullet).setAttribute("rocket");
        ((ThrownItemEntityInterface) bullet).setDamage(explosionPower);
        bullet.setVelocity(VMath.applyDivergence(user.getRotationVector(), divergence).normalize().multiply(speed));
        bullet.setItem(new ItemStack(net.minecraft.item.Items.TNT));
        world.spawnEntity(bullet);
        return bullet;
    }

    public static void shootFireball(ItemStack itemStack, PlayerEntity user) {
        if (!((LivingEntityInterface) user).getUsing()) {
            boolean c = user.isCreative();
            Vec3d v = user.getRotationVector();
            boolean bl = user.isSneaking();
            int power = user.isSneaking() ? 2 : 1;
            if (!c) itemStack.damage(bl ? 3 : 1, user.getRandom(), (ServerPlayerEntity) user);
            FireballEntity f = new FireballEntity(user.world, user, v.x, v.y, v.z, power);
            f.setPosition(user.getEyePos().add(v.multiply(0.4)));
            f.setVelocity(v.multiply(2));
            Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_BLAZE_SHOOT, 1.0f, 0);
            ((ExplosiveProjectileEntityInterface) f).limitLifetime(true);
            user.world.spawnEntity(f);
            ((ItemStackInterface) (Object) itemStack).setCooldown(bl ? 60 : 20);
        }
    }

    public static void flamethrower(ItemStack itemStack, PlayerEntity user) {
        if (!((LivingEntityInterface) user).getUsing()) {
            boolean c = user.isCreative();
            if (!c) itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
            boolean bl = user.isSneaking();
            Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_BLAZE_SHOOT, 0.8f, 0);
            for (int i = 0; i < (bl ? 6 : 4); i++) spewFire(user.world, user, bl ? 1.4f : 1.2f, bl ? 7.5f : 10.0f);
            ((ItemStackInterface) (Object) itemStack).setCooldown(3);
        }
    }

    public static boolean arrowHitscan(ServerWorld world, int limit, PersistentProjectileEntity a, Entity user) {
        Vec3d pos = a.getPos();
        Vec3d velocity = a.getVelocity();
        BlockPos b; VoxelShape vs; Box box; BlockState blockState;
        float drag, height;
        boolean particleCheck;
        Vec3d v = new Vec3d(0.25, 0.25, 0.25);
        for (int iter = 0; iter < limit; iter++) {
            drag = 0.99f;
            for (int i = 0; i < 8; i++) {
                b = new BlockPos(pos);
                box = new Box(pos.subtract(v), pos.add(v));
                blockState = world.getBlockState(b);
                vs = blockState.getCollisionShape(world, b, ShapeContext.of(user));
                particleCheck = i % (6 / ((int)velocity.length() + 1)) == 0;
                if (!vs.isEmpty() && vs.getBoundingBox().offset(b).intersects(box)) {
                    VMath.updatePos(a, pos, velocity);
                    return false;
                } else if (!blockState.getFluidState().isEmpty() && (blockState.getFluidState().isOf(Fluids.WATER) || blockState.getFluidState().isOf(Fluids.FLOWING_WATER))) {
                    height = blockState.getFluidState().getHeight();
                    if (particleCheck) Network.spawnParticles(world, ParticleTypes.BUBBLE, pos.x, pos.y, pos.z, 2, 0.1, 0.1, 0.1, 0);
                    if (box.intersects(new Box(b).shrink(0, 1 - height, 0))) drag = 0.6f;
                }
                if (particleCheck) Network.spawnParticles(world, ParticleTypes.PORTAL, pos.x, pos.y - 0.7, pos.z, 7, 0.1, 0.1, 0.1, 0);
                for (Entity e : world.getOtherEntities(user, box, (e) -> (e instanceof LivingEntity || e instanceof BoatEntity || e instanceof MinecartEntity))) {
                    int damage = net.minecraft.util.math.MathHelper.ceil(net.minecraft.util.math.MathHelper.clamp(velocity.length() * a.getDamage(), 0.0D, 2.147483647E9D));
                    long l = EchoShardRecipesMod.ECHO_SHARD_RANDOM.nextInt(damage / 2 + 2);
                    damage = (int) Math.min(l + (long) damage, 2147483647L);
                    a.setPosition(pos);
                    if (a.isOnFire()) e.setOnFireFor(5);
                    if (!a.world.isClient && a.getPierceLevel() <= 0 && e instanceof LivingEntity) ((LivingEntity)e).setStuckArrowCount(((LivingEntity)e).getStuckArrowCount() + 1);
                    if (e.damage(DamageSource.arrow(a, user), damage)) return true;
                    else {
                        VMath.updatePos(a, pos, velocity);
                        return false;
                    }
                }
                pos = pos.add(velocity.multiply(0.125));
            }
            velocity = velocity.multiply(drag).add(0, -0.05000000074505806D,0);
        }
        a.setPosition(pos.subtract(velocity));
        a.setVelocity(velocity);
        a.setYaw((float)(net.minecraft.util.math.MathHelper.atan2(velocity.x, velocity.z) * 57.2957763671875D));
        a.setPitch((float)(net.minecraft.util.math.MathHelper.atan2(velocity.y, velocity.horizontalLength()) * 57.2957763671875D));
        return false;
    }

}
