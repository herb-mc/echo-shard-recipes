package com.herb_mc.echo_shard_recipes.helper;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HelperMethods {

    public static String getAttribute(ItemStack i) {
        if (!i.getOrCreateNbt().getBoolean(EchoShardRecipesMod.HAS_ATTRIBUTE)) return "";
        return i.getOrCreateNbt().getString(EchoShardRecipesMod.ATTRIBUTE);
    }

    public static void removeAttribute(LivingEntity entity, EntityAttribute attribute, UUID uuid){
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null && instance.getModifier(uuid) != null)
            instance.removeModifier(uuid);
    }

    public static void addAttribute(LivingEntity e, EchoShardRecipesMod.AttributeItem i) {
        if (i != null) {
            EntityAttributeInstance instance = e.getAttributeInstance(i.attribute);
            if (instance != null && i.uuid != null) instance.addTemporaryModifier(new EntityAttributeModifier(i.uuid, i.tag, i.base, i.op));
        }
    }

    public static void addAttribute(LivingEntity e, EchoShardRecipesMod.AttributeItem i, double base) {
        EntityAttributeInstance instance = e.getAttributeInstance(i.attribute);
        if (instance != null && i.uuid != null)
            instance.addTemporaryModifier(new EntityAttributeModifier(i.uuid, i.tag, base, i.op));
    }

    public static void addAttribute(LivingEntity l, EntityAttribute e, UUID uuid, String tag, double base, EntityAttributeModifier.Operation op) {
        EntityAttributeInstance instance = l.getAttributeInstance(e);
        if (instance != null && uuid != null)
            instance.addTemporaryModifier(new EntityAttributeModifier(uuid, tag, base, op));
    }

    public static void spawnFrag(World world, LivingEntity user, float bonus, float speed,  float divergence) {
        SnowballEntity frag = new SnowballEntity(world, user);
        ((ThrownItemEntityInterface) frag).setAttribute("buckshot");
        ((ThrownItemEntityInterface) frag).setBonusDamage(bonus);
        frag.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, speed, divergence);
        frag.setVelocity(user.getVelocity().multiply(0.1).add(frag.getVelocity()));
        frag.setItem(new ItemStack(Items.IRON_NUGGET));
        world.spawnEntity(frag);
    }

    public static void spewFire(World world, LivingEntity user, float speed,  float divergence) {
        SnowballEntity fireball = new SnowballEntity(world, user);
        ((ThrownItemEntityInterface) fireball).setAttribute("flamethrower");
        fireball.setFireTicks(20000);
        fireball.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, speed, divergence);
        fireball.setVelocity(user.getVelocity().multiply(0.1).add(fireball.getVelocity()));
        fireball.setItem(new ItemStack(Items.FIRE_CHARGE));
        world.spawnEntity(fireball);
    }

    public static boolean isInorganic(LivingEntity e) {
        return e instanceof ShulkerEntity || e instanceof IronGolemEntity || e instanceof SkeletonEntity || e instanceof WitherSkeletonEntity || e instanceof WitherEntity || e instanceof BlazeEntity || e instanceof SkeletonHorseEntity || e instanceof StrayEntity || e instanceof EndermanEntity;
    }

    public static void spawnParticles(ServerWorld world, ParticleEffect particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed) {
        ParticleS2CPacket particleS2CPacket = new ParticleS2CPacket(particle, true, x, y, z, (float)deltaX, (float)deltaY, (float)deltaZ, (float)speed, count);
        for(int j = 0; j < world.getPlayers().size(); ++j)
            sendToPlayerWithinRenderDistance(world, world.getPlayers().get(j), x, y, z, particleS2CPacket);
    }

    public static void playSound(ServerWorld world, LivingEntity entity, SoundEvent s, float volume, float pitch) {
        PlaySoundFromEntityS2CPacket packet = new PlaySoundFromEntityS2CPacket(s, SoundCategory.MASTER, entity, volume, pitch, 102);
        for(int j = 0; j < world.getPlayers().size(); ++j)
            sendToPlayerInRange(world, world.getPlayers().get(j), entity.getX(), entity.getY(), entity.getZ(), packet, 40);
    }

    private static void sendToPlayerWithinRenderDistance(ServerWorld world, ServerPlayerEntity player, double x, double y, double z, Packet<?> packet) {
        if (player.getWorld() != world) return;
        else {
            BlockPos blockPos = player.getBlockPos();
            if (blockPos.isWithinDistance(new Vec3d(x, y, z), 128.0D))
                player.networkHandler.sendPacket(packet);
        }
    }

    public static void sendToPlayerInRange(ServerWorld world, ServerPlayerEntity player, double x, double y, double z, Packet<?> packet, double range) {
        if (player.getWorld() != world) return;
        else {
            BlockPos blockPos = player.getBlockPos();
            if (blockPos.isWithinDistance(new Vec3d(x, y, z), range))
                player.networkHandler.sendPacket(packet);
        }
    }

    public static List<ItemEntity> getNearestItems(LivingEntity livingEntity, double dist) {
        List<ItemEntity> list = livingEntity.world.getEntitiesByClass(ItemEntity.class, livingEntity.getBoundingBox().expand(dist), EntityPredicates.VALID_ENTITY);
        List<ItemEntity> finalList = new ArrayList<>();
        double sqDist = dist * dist;
        if (!list.isEmpty()) for (ItemEntity entity : list) if (getSquareDist(entity.getPos(), livingEntity.getPos()) < sqDist) finalList.add(entity);
        return finalList;
    }

    private static double getSquareDist(Vec3d in1, Vec3d in2){
        in2 = in2.subtract(in1);
        return in2.x * in2.x + in2.y * in2.y + in2.z * in2.z;
    }

}
