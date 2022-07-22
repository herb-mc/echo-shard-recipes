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
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import static com.herb_mc.echo_shard_recipes.helper.Misc.*;
import static com.herb_mc.echo_shard_recipes.helper.Network.playSound;

public class ProjectileHelper {

    public interface OnUse {
        boolean onUse(PlayerEntity p, ItemStack i, boolean b);
    }

    public static class ProjectileItem {

        public Item ammo;
        String attribute;
        public OnUse use;
        public BiPredicate<PlayerEntity, ItemStack> predicate = ALWAYS_FAIL;

        public ProjectileItem(Item a, String at, OnUse onUse) {
            ammo = a;
            attribute = at;
            use = onUse;
        }

        public ProjectileItem(Item a, String at, OnUse onUse, BiPredicate<PlayerEntity, ItemStack> p ) {
            this(a, at, onUse);
            predicate = p;
        }

    }

    public static BiPredicate<PlayerEntity, ItemStack> ALWAYS_FAIL = (p, i) -> false;
    public static BiPredicate<PlayerEntity, ItemStack> NOT_USING = (p, i) -> (((PlayerEntityInterface) p).hasNoCooldown() && i == ((PlayerEntityInterface) p).getLastStack());
    public static BiPredicate<PlayerEntity, ItemStack> QUICK_DRAW = (p, i) -> (Items.IRON_HOE.equals(i.getItem()) && !((LivingEntityInterface) p).getUsing());

    public static OnUse NO_ACTION = (user, itemStack, isSneaking) -> (false);
    public static OnUse BURST = (user, itemStack, isSneaking) -> {
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
        createProjectile(user.world, user, true, "gun_ho", 4.0f, 0.0174533, 5.0f, Items.GOLD_NUGGET, 0.6f);
        postShoot(user, itemStack, 0.1, 8);
        ((LivingEntityInterface) user).setBurst(4, itemStack);
        return true;
    };
    public static OnUse AUTO = (user, itemStack, isSneaking) -> {
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
        createProjectile(user.world, user, true, "gun_ho", 4.0f, 0.0174533 * (isSneaking ? 1 : 2.5), 6.0f, Items.GOLD_NUGGET, 0.5f);
        postShoot(user, itemStack, isSneaking ? 0.05 : 0.1, 3);
        return true;
    };
    public static OnUse REVOLVER = (user, itemStack, isSneaking) -> {
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ITEM_CROSSBOW_SHOOT, 0.8f, 0.4f);
        createProjectile(user.world, user, true, "gun_ho", 4.0f, 0.0174533 * (isSneaking ? 1.5 : 4.0), 6.0f, Items.GOLD_NUGGET, 1.0f);
        postShoot(user, itemStack, isSneaking ? 0.075 : 0.125, 0);
        ((ItemStackInterface) (Object) itemStack).setCooldown(8);
        return true;
    };
    public static OnUse SHOTGUN = (user, itemStack, isSneaking) -> {
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
        for (int i = 0; i < user.getRandom().nextBetween(12, 16); i++)
            createProjectile(user.world, user, true, "gun_ho", 3.0f, 0.0174533 * 6, 2.2f, Items.GOLD_NUGGET, 0.9f);
        postShoot(user, itemStack, isSneaking ? 0.4 : 1, 35);
        return true;
    };
    public static OnUse SNIPER = (user, itemStack, isSneaking) -> {
        playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f, 2);
        playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
        playSound((ServerWorld) user.world, user, SoundEvents.ITEM_TOTEM_USE, 0.35f, 2);
        playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 1.8f);
        double div = 0;
        if (!user.isSneaking()) div += 0.01;
        if (!user.isOnGround()) div += 0.01;
        if (user.isOnFire()) div += 0.03;
        Vec3d dir = user.getRotationVector().normalize();
        if (div > 0) dir = VMath.applyDivergence(dir, div);
        dir = dir.multiply(0.2);
        RaycastHit r = raycastToFirstHit((ServerWorld) user.world, user, user.getEyePos(), dir, new Vec3d(0.15, 0.15, 0.15), 500, 3, new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.GOLD_BLOCK.getDefaultState()), 0, 0, 0, 0.05);
        if (r.type == RaycastType.ENTITY) {
            boolean headshot = isSneaking && user.isOnGround() && r.endPos.squaredDistanceTo(r.entityHit.getEyePos()) < Math.pow((r.entityHit.getBoundingBox().maxX - r.entityHit.getBoundingBox().minX) / 1.2, 2);
            Entities.damageEntity(r.entityHit, headshot ? 42 : 21, headshot ? 50 : 25, true, DamageSource.thrownProjectile(user, user));
        } else if (r.type == RaycastType.BLOCK) Network.emitBlockBreakParticles(user.world, r.blockHit);
        postShoot(user, itemStack, isSneaking ? 0.07 : 0.3, 45);
        return true;
    };
    public static OnUse ROCKET = (user, itemStack, isSneaking) -> {
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_TNT_PRIMED, 0.6f, 2);
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.4f);
        Network.playSound((ServerWorld) user.world, user, SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0f, 1.5f);
        createProjectile(user.world, user, true, "rocket", 1.0f, 0.0174533 * 1.5, 4, Items.TNT, 0.25f);
        postShoot(user, itemStack, 1.4, 80);
        return true;
    };

    public static Map<Item, ProjectileItem> PROJECTILE_ITEMS = new HashMap<>();
    public static ProjectileItem DEFAULT = new ProjectileItem(Items.BEDROCK, "", NO_ACTION);

    static {
        PROJECTILE_ITEMS.put(Items.NETHERITE_HOE, new ProjectileItem(Items.GOLD_NUGGET, "gun_ho", BURST, NOT_USING));
        PROJECTILE_ITEMS.put(Items.DIAMOND_HOE, new ProjectileItem(Items.GOLD_NUGGET, "gun_ho", AUTO, NOT_USING));
        PROJECTILE_ITEMS.put(Items.IRON_HOE, new ProjectileItem(Items.GOLD_NUGGET, "gun_ho", REVOLVER, QUICK_DRAW));
        PROJECTILE_ITEMS.put(Items.STONE_HOE, new ProjectileItem(Items.GOLD_INGOT, "gun_ho", SHOTGUN, NOT_USING));
        PROJECTILE_ITEMS.put(Items.WOODEN_HOE, new ProjectileItem(Items.GOLD_INGOT, "gun_ho", SNIPER, NOT_USING));
        PROJECTILE_ITEMS.put(Items.GOLDEN_HOE, new ProjectileItem(Items.GUNPOWDER, "rocket", ROCKET, NOT_USING));
    }

    public static boolean consumeAmmo(PlayerEntity user, Item ammo, ItemStack itemStack, boolean c) {
        ItemStack ammoStack = ItemHelper.hasItem(user.getInventory(), ammo);
        if (!c && ammoStack != null) {
            itemStack.damage(1, user.getRandom(), (ServerPlayerEntity) user);
            ItemHelper.decrement(ammoStack);
            return true;
        }
        return c;
    }

    public static void postShoot(PlayerEntity user, ItemStack itemStack, double recoil, int cooldown) {
        Vec3d rot = user.getRotationVector().multiply(-recoil);
        user.addVelocity(rot.x, rot.y, rot.z);
        ((ServerPlayerEntity) user).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(user));
        if (cooldown != 0) ItemHelper.addCooldown(user, itemStack.getItem(), cooldown);
        ((LivingEntityInterface) user).setUsing(true);
    }

    public static boolean gunShoot(ItemStack itemStack, PlayerEntity user) {
        ProjectileItem p = PROJECTILE_ITEMS.getOrDefault(itemStack.getItem(), DEFAULT);
        return p.predicate.test(user, itemStack) && consumeAmmo(user, p.ammo, itemStack, user.isCreative()) && p.use.onUse(user, itemStack, user.isSneaking());
    }

    public static void spawnFrag(World world, LivingEntity user, float bonus, float speed, float divergence) {
        SnowballEntity frag = new SnowballEntity(world, user);
        ((ThrownItemEntityInterface) frag).setAttribute("buckshot");
        ((ThrownItemEntityInterface) frag).setBonusDamage(bonus);
        frag.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, speed, divergence);
        frag.setVelocity(user.getVelocity().multiply(0.1).add(frag.getVelocity()));
        frag.setItem(new ItemStack(Items.IRON_NUGGET));
        world.spawnEntity(frag);
    }

    public static SnowballEntity createProjectile(World world, LivingEntity user, boolean noGravity, String attribute, float speed, double divergence, float damage, Item item, float increment) {
        SnowballEntity bullet = new SnowballEntity(world, user);
        bullet.setNoGravity(noGravity);
        ((ThrownItemEntityInterface) bullet).setAttribute(attribute);
        ((ThrownItemEntityInterface) bullet).setDamage(damage);
        ((ThrownItemEntityInterface) bullet).setIncrement(increment);
        bullet.setVelocity(VMath.applyDivergence(user.getRotationVector(), divergence).normalize().multiply(speed));
        bullet.setItem(new ItemStack(item));
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

    public static void spewFire(World world, LivingEntity user, float speed, float divergence) {
        SnowballEntity fireball = new SnowballEntity(world, user);
        ((ThrownItemEntityInterface) fireball).setAttribute("flamethrower");
        fireball.setFireTicks(20000);
        fireball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, speed, divergence);
        fireball.setVelocity(user.getVelocity().multiply(0.1).add(fireball.getVelocity()));
        fireball.setItem(new ItemStack(Items.FIRE_CHARGE));
        world.spawnEntity(fireball);
    }

    public static boolean arrowHitscan(ServerWorld world, int limit, PersistentProjectileEntity a, Entity user) {
        Vec3d pos = a.getPos();
        Vec3d velocity = a.getVelocity();
        BlockPos b; VoxelShape vs; Box box; BlockState blockState; float drag, height; boolean particleCheck;
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