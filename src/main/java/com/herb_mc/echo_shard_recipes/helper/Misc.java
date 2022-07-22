package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.ECHO_SHARD_RANDOM;
import static com.herb_mc.echo_shard_recipes.helper.VMath.applyDivergenceDeg;

public class Misc {

    enum RaycastType{
        BLOCK,
        ENTITY,
        FAIL
    }

    public static class RaycastHit {

        RaycastType type = RaycastType.FAIL;
        Entity entityHit = null;
        BlockPos blockHit = null;
        Vec3d endPos = null;

        public RaycastHit(){}

        public RaycastHit(Entity e, @Nullable Vec3d v) {
            entityHit = e;
            type = RaycastType.ENTITY;
            endPos = v;
        }

        public RaycastHit(BlockPos b) {
            blockHit = b;
            type = RaycastType.BLOCK;
        }

    }

    public static RaycastHit raycastToFirstHit(ServerWorld world, Entity user, Vec3d pos, Vec3d dir, Vec3d collisionSize, int max, int particleFrequency, @Nullable ParticleEffect p, double deltaX, double deltaY, double deltaZ, double speed) {
        BlockPos b;
        VoxelShape vs;
        Box box;
        for (int iter = 0; iter < max; iter++) {
            b = new BlockPos(pos);
            vs = world.getBlockState(b).getCollisionShape(world, b, ShapeContext.of(user));
            if (!vs.isEmpty() && vs.getBoundingBox().offset(b).contains(pos)) return new RaycastHit(b);
            if (iter % particleFrequency == 0 && p != null) Network.spawnParticles(world, p, pos.x, pos.y, pos.z, 1, deltaX, deltaY, deltaZ, speed);
            box = new Box(pos.subtract(collisionSize), pos.add(collisionSize));
            for (Iterator<Entity> it = world.getOtherEntities(user, box, (e) -> (true)).iterator(); it.hasNext();) return new RaycastHit(it.next(), pos);
            pos = pos.add(dir);
        }
        return new RaycastHit();
    }

    public static MutableText getText(String type, String value, Formatting f) {
        return MutableText.of(Text.of(type + ": [").getContent()).setStyle(Style.EMPTY.withFormatting(Formatting.WHITE).withItalic(false).withBold(false))
                .append(MutableText.of(Text.of(value).getContent()).setStyle(Style.EMPTY.withFormatting(f).withItalic(false).withBold(false)))
                .append(MutableText.of(Text.of("]").getContent()).setStyle(Style.EMPTY.withFormatting(Formatting.WHITE).withItalic(false).withBold(false)));
    }

}
