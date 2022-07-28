package com.herb_mc.echo_shard_recipes.helper;

import com.herb_mc.echo_shard_recipes.api.LivingEntityInterface;
import com.herb_mc.echo_shard_recipes.api.ManaPlayer;
import com.herb_mc.echo_shard_recipes.api.PersistentProjectileEntityInterface;
import com.herb_mc.echo_shard_recipes.api.ThrownItemEntityInterface;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;
import static com.herb_mc.echo_shard_recipes.helper.AttributeHelper.getAttribute;
import static com.herb_mc.echo_shard_recipes.helper.Misc.raycastToFirstHit;
import static com.herb_mc.echo_shard_recipes.helper.VMath.*;
import static com.herb_mc.echo_shard_recipes.helper.Network.*;

import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings({"UnusedReturnValue", "ConstantConditions"})
public class Spells {

    public static final String SPELL = "StoredSpell";
    public static HashMap<String, WandItem> WANDS = new HashMap<>();

    public interface Spell {
        boolean spell(PlayerEntity p, double b);
    }

    public static class WandItem {

        public Item item;
        public String name;
        public Formatting color;
        public AttributeHelper.PostProcess processor;
        public int cooldown;
        public double manaCost;
        public Spell spell;

        public WandItem(Item i, String s, Formatting f, int c, double d, Spell sp, AttributeHelper.PostProcess p) {
            item = i;
            name = s;
            color = f;
            processor = p;
            cooldown = c;
            manaCost = d;
            spell = sp;
        }


        public WandItem(Item i, String s, Formatting f, int c, double d, Spell sp) {
            this(i, s, f, c, d, sp, AttributeHelper.ENCHANT_GLINT);
        }

    }

    public static final Spell LIGHTNING = (p, b) -> {
        lightningRaycast((ServerWorld) p.world, 10, p.getEyePos(), applyDivergenceDeg(p.getRotationVector(), 0).normalize().multiply(0.2), p, 0, 4, b);
        playSound((ServerWorld) p.world, p, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 1.2f);
        playSound((ServerWorld) p.world, p, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1.6f, 2.0f);
        if (!p.isCreative()) p.getHungerManager().addExhaustion(0.4f);
        return true;
    };
    public static final Spell BOOST = (p, b) -> {
        playSound((ServerWorld) p.world, p, SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.6f, 1.3f);
        p.setVelocity(p.getRotationVector().multiply(1.2 * (1 + b)).add(p.getVelocity()));
        if (p.getVelocity().y > 0) p.fallDistance = 0;
        if (p instanceof ServerPlayerEntity && !p.world.isClient())
            ((ServerPlayerEntity) p).networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(p));
        if (!p.isCreative()) p.getHungerManager().addExhaustion(0.1f);
        return true;
    };
    public static final Spell SACRIFICE = (p, b) -> {
        playSound((ServerWorld) p.world, p, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.5f);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, (int) (200 * (1 + b)), 2));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, (int) (200 * (1 + b)), 2));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, (int) (200 * (1 + b)), 0));
        p.getHungerManager().addExhaustion(1.0f);
        if (!p.isCreative()) p.damage(DamageSource.MAGIC, 10.0f);
        return true;
    };
    public static final Spell ARROW_RAIN = (p, b) -> {
        playSound((ServerWorld) p.world, p, SoundEvents.ENTITY_ARROW_SHOOT, 1.0f, 0.5f);
        for (int i = 0; i < ECHO_SHARD_RANDOM.nextInt(8, 13); i++) {
            ArrowEntity a = new ArrowEntity(p.world, p);
            a.setDamage(1.0);
            ((PersistentProjectileEntityInterface) a).setIgnoresIframes(true);
            a.setVelocity(applyDivergenceDeg(p.getRotationVector(), 15.0).multiply(1.25 * b + ECHO_SHARD_RANDOM.nextFloat(1.4f,2.0f)).add(p.getVelocity()));
            a.setCritical(true);
            p.world.spawnEntity(a);
            if (!p.isCreative()) p.getHungerManager().addExhaustion(0.0125f);
        }
        return true;
    };
    public static final Spell HEALING = (p, b) -> {
        playSound((ServerWorld) p.world, p, SoundEvents.ENTITY_PLAYER_BURP, 1.0f, 1.5f);
        p.heal((float) (2.0f * (1 + b)));
        if (!p.isCreative()) p.getHungerManager().addExhaustion(3.0f);
        return true;
    };
    public static final Spell SUNBEAM = (p, b) -> {
        Misc.RaycastHit r = raycastToFirstHit((ServerWorld) p.world, p, p.getEyePos(), p.getRotationVector().multiply(0.2), new Vec3d(0.15, 0.15, 0.15), 125, 3, ParticleTypes.FLAME, 0.1, 0.1, 0.1, 0.05);
        BlockPos pos = null;
        Network.playSound((ServerWorld) p.world, p, SoundEvents.BLOCK_FIRE_EXTINGUISH, 2.0f, 0.6f);
        if (r.type == Misc.RaycastType.ENTITY) pos = new BlockPos(r.entityHit.getPos());
        else if (r.type == Misc.RaycastType.BLOCK) pos = r.blockHit;
        if (pos == null) return false;
        for (int y = pos.getY() + 1; y < p.world.getHeight(); y++) if (p.world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).isOpaque()) return false;
        SnowballEntity s = new SnowballEntity(p.world, p);
        s.setItem(new ItemStack(Items.SUNFLOWER));
        s.setPosition(new Vec3d(pos.getX() + 0.5, pos.getY() + 40, pos.getZ() + 0.5));
        if (!p.isCreative()) p.getHungerManager().addExhaustion(2.0f);
        ((ThrownItemEntityInterface) s).setAttribute("sunbeam");
        p.world.spawnEntity(s);
        return true;
    };
    public static final Spell GUARDIAN = (p, b) -> {
        playSound((ServerWorld) p.world, p, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 0.5f);
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, (int) (200 * (1 + b)), 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, (int) (200 * (1 + b)), 0));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, (int) (200 * (1 + b)), 1));
        p.getHungerManager().addExhaustion(2.0f);
        return true;
    };

    static {
        WANDS.put("arrow_rain", new WandItem(Items.ARROW, "Arrow Rain", Formatting.AQUA, 15, 3, ARROW_RAIN));
        WANDS.put("boost", new WandItem(Items.FEATHER, "Boost", Formatting.GREEN, 5, 0.75, BOOST));
        WANDS.put("healing", new WandItem(Items.GOLDEN_APPLE, "Healing", Formatting.RED, 30, 2.5, HEALING));
        WANDS.put("guardian", new WandItem(Items.SHIELD, "Guardian", Formatting.GRAY, 80, 8, GUARDIAN));
        WANDS.put("lightning", new WandItem(Items.LIGHTNING_ROD, "Lightning", Formatting.YELLOW, 40, 3, LIGHTNING));
        WANDS.put("sacrifice", new WandItem(Items.ROTTEN_FLESH, "Sacrifice", Formatting.DARK_RED, 80, 8, SACRIFICE));
        WANDS.put("sunbeam", new WandItem(Items.SUNFLOWER, "Sunbeam", Formatting.YELLOW, 80, 5, SUNBEAM));
    }

    public static void tickMana(PlayerEntity p, ServerBossBar bossBar) {
        if (!p.isDead()) {
            ManaPlayer mp = (ManaPlayer) p;
            double mana = mp.getMana();
            double boost = 0;
            double manaBoost = 0;
            for (ItemStack i : p.getArmorItems()) if ("energized".equals(getAttribute(i))) boost += 0.005;
            if (boost == 0.02) boost += 0.005;
            for (ItemStack i : p.getArmorItems()) if ("archmage".equals(getAttribute(i))) {
                boost += 0.015;
                manaBoost += 10;
            }
            mp.setMaxManaBoost(0);
            mp.addMaxManaBoost(manaBoost);
            mp.addManaRegenBoost(boost);
            mp.addMana(mp.getManaRegen());
            mp.setManaRegenBoost(0);
            double maxMana = mp.getMaxMana();
            bossBar.setPercent((float) (mp.getMana() / maxMana));
            bossBar.setName(Text.of("Mana: " + Math.round(mana * 10.0) / 10.0 + "/" + Math.round(maxMana * 10.0) / 10.0));
            if (mana < maxMana || !"".equals(getSpell(p.getMainHandStack())) || !"".equals(getSpell(p.getOffHandStack())))
                bossBar.addPlayer((ServerPlayerEntity) p);
            else bossBar.removePlayer((ServerPlayerEntity) p);
        } else bossBar.removePlayer((ServerPlayerEntity) p);
    }

    public static String getSpell(ItemStack i) {
        NbtCompound nbt = i.getNbt();
        return nbt != null ? nbt.getString(SPELL) : "";
    }

    public static boolean processSpell(ItemStack i, PlayerEntity p) {
        if (!p.world.isClient() && !((LivingEntityInterface) p).getUsing() && WANDS.containsKey(getSpell(i))) {
            double boost = 0;
            double cooldownMod = 1;
            for (ItemStack d : p.getArmorItems()) switch(getAttribute(d)) {
                case "scholarly" -> boost += 0.05;
                case "adept" -> cooldownMod += 0.2;
            }
            if (boost == 0.2) boost += 0.05;
            if (cooldownMod == 1.8) cooldownMod += 0.2;
            for (ItemStack d : p.getArmorItems()) if ("archmage".equals(getAttribute(d))) {
                boost += 0.1;
                cooldownMod += 0.4;
            }
            WandItem wand = WANDS.get(getSpell(i));
            if (((ManaPlayer) p).tryRemoveMana(wand.manaCost)) {
                wand.spell.spell(p, boost);
                if (p.getHungerManager().getFoodLevel() == 0) p.damage(DamageSource.STARVE, 2);
                ((LivingEntityInterface) p).setUsing(true);
                p.getItemCooldownManager().set(Items.STICK, (int) (wand.cooldown / cooldownMod));
                return true;
            }
        }
        return false;
    }

    private static void lightningRaycast(ServerWorld world, int limit, Vec3d pos, Vec3d dir, Entity user, int depth, int branches, double d) {
        BlockPos b;
        VoxelShape vs;
        Box box;
        ParticleEffect p = ParticleTypes.ELECTRIC_SPARK;
        Vec3d v = new Vec3d(0.15, 0.15, 0.15);
        for (int iter = 0; iter < limit; iter++) {
            b = new BlockPos(pos);
            vs = world.getBlockState(b).getCollisionShape(world, b, ShapeContext.of(user));
            if (!vs.isEmpty() && vs.getBoundingBox().offset(b).contains(pos)) return;
            if (iter % 3 == 0) Network.spawnParticles(world, p, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0.05, true);
            box = new Box(pos.subtract(v), pos.add(v));
            for (Entity e : world.getOtherEntities(user, box, (e) -> (true))) {
                if ((e instanceof LivingEntity l && l.getHealth() > 0))
                    Entities.damageEntity(e, (float) (1.0f + d), (float) (0.1f + d), true, new ProjectileDamageSource("indirectMagic", new LightningEntity(EntityType.LIGHTNING_BOLT, world), user));
            }
            pos = pos.add(dir);
        }
        if (depth < 10) for (int i = 0; i < branches; i++) lightningRaycast(world, 20, pos, applyDivergenceDeg(dir, 20).multiply(0.2), user, depth + ECHO_SHARD_RANDOM.nextInt(1, 3), ECHO_SHARD_RANDOM.nextInt(branches - 2, branches + ECHO_SHARD_RANDOM.nextInt(0, 2)), d);
    }

}
