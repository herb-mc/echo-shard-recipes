package com.herb_mc.echo_shard_recipes.helper;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("SuspiciousNameCombination")
public class VMath {

    private static final Vec3d AXIS_X = new Vec3d(1, 0, 0);
    private static final Vec3d AXIS_Y = new Vec3d(0, 1, 0);

    static double getSquareDist(Vec3d in1, Vec3d in2){
        in2 = in2.subtract(in1);
        return in2.x * in2.x + in2.y * in2.y + in2.z * in2.z;
    }

    public static Vec3d applyDivergence(Vec3d in, double maxAngle) {
        in = in.normalize();
        return rotateAbout(rotateAbout(in, (in.y == 1 || in.y == -1 ? AXIS_X : AXIS_Y).crossProduct(in).normalize(),
                        EchoShardRecipesMod.ECHO_SHARD_RANDOM.nextDouble() * maxAngle), in,
                        EchoShardRecipesMod.ECHO_SHARD_RANDOM.nextDouble() * Math.PI * 2).normalize();
    }

    public static Vec3d applyDivergenceDeg(Vec3d in, double maxAngle) {
        return applyDivergence(in, maxAngle * 2 * Math.PI / 360);
    }

    private static Vec3d rotateAbout(Vec3d base, Vec3d axis, double angle) {
        return base.multiply(Math.cos(angle))
                .add(axis.crossProduct(base).multiply(Math.sin(angle)))
                .add(axis.multiply(axis.dotProduct(base)).multiply(1 - Math.cos(angle)));
    }

    static void updatePos(PersistentProjectileEntity a, Vec3d pos, Vec3d velocity) {
        a.setPosition(pos.subtract(velocity));
        a.setVelocity(velocity);
        a.setYaw((float)(net.minecraft.util.math.MathHelper.atan2(velocity.x, velocity.z) * 57.2957763671875D));
        a.setPitch((float)(net.minecraft.util.math.MathHelper.atan2(velocity.y, velocity.horizontalLength()) * 57.2957763671875D));
    }

}
