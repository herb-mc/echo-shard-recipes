package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Formatting;

public class ParticleHelper {

    public static class ParticleItem {

        public Item item;
        public String string;
        public ParticleEffect particle;
        public int particleCount;
        public float offsetScale;
        public Formatting color;
        public float hardOffsetX;
        public float hardOffsetY;
        public float hardOffsetZ;

        ParticleItem(Item i, String s, ParticleEffect p, int c, float f, Formatting t) {
            item = i;
            string = s;
            particle = p;
            particleCount = c;
            offsetScale = f;
            color = t;
            hardOffsetX = 0;
            hardOffsetY = 0;
            hardOffsetZ = 0;
        }

        ParticleItem(Item i, String s, ParticleEffect p, int c, float f, Formatting t, float x, float y, float z) {
            this(i, s, p, c, f, t);
            hardOffsetX = x;
            hardOffsetY = y;
            hardOffsetZ = z;
        }

    }

    public static final String HAS_PARTICLE = "HasShardParticleEffect";
    public static final String PARTICLE = "ShardParticleEffect";
    public static final ParticleItem[] PARTICLE_ITEMS = {
            new ParticleItem(Items.BONE_MEAL, "Growth", ParticleTypes.COMPOSTER, 15, 0.6f, Formatting.GREEN),
            new ParticleItem(Items.COPPER_INGOT, "Oxidation", ParticleTypes.SCRAPE, 10, 1.2f, Formatting.DARK_AQUA),
            new ParticleItem(Items.END_ROD, "End Rod", ParticleTypes.END_ROD, 4, 0.3f, Formatting.WHITE),
            new ParticleItem(Items.ENDER_PEARL, "Teleport", ParticleTypes.PORTAL, 30, 1.2f, Formatting.DARK_PURPLE,0, -0.8f, 0),
            new ParticleItem(Items.FIREWORK_ROCKET, "Firework", ParticleTypes.FIREWORK, 4, 0.3f, Formatting.WHITE),
            new ParticleItem(Items.GLOW_INK_SAC, "Glow Ink", ParticleTypes.GLOW_SQUID_INK, 3, 0.6f, Formatting.DARK_AQUA),
            new ParticleItem(Items.GOLDEN_APPLE, "Heart", ParticleTypes.HEART, 1, 0.8f, Formatting.RED),
            new ParticleItem(Items.HONEYCOMB, "Wax", ParticleTypes.WAX_ON, 10, 1.2f, Formatting.YELLOW),
            new ParticleItem(Items.LAVA_BUCKET, "Lava", ParticleTypes.LAVA, 6, 0.6f, Formatting.YELLOW),
            new ParticleItem(Items.INK_SAC, "Ink", ParticleTypes.SQUID_INK, 3, 0.6f, Formatting.DARK_GRAY),
            new ParticleItem(Items.POTION, "Witch", ParticleTypes.WITCH, 8, 0.0f, Formatting.DARK_PURPLE),
            new ParticleItem(Items.REDSTONE, "Redstone", DustParticleEffect.DEFAULT, 15, 0.6f, Formatting.RED),
            new ParticleItem(Items.SCULK, "Sculk", ParticleTypes.SCULK_CHARGE_POP, 6, 1.2f, Formatting.DARK_AQUA),
            new ParticleItem(Items.SLIME_BALL, "Slime", ParticleTypes.ITEM_SLIME, 8, 0.0f, Formatting.GREEN),
            new ParticleItem(Items.SNOWBALL, "Snow", ParticleTypes.ITEM_SNOWBALL, 8, 0.2f, Formatting.WHITE),
            new ParticleItem(Items.SOUL_TORCH, "Soul Fire", ParticleTypes.SOUL_FIRE_FLAME, 8, 0.2f, Formatting.AQUA),
            new ParticleItem(Items.TORCH, "Fire", ParticleTypes.SMALL_FLAME, 8, 0.2f, Formatting.YELLOW)
    };
    public static Item[] validParticleItems = {Items.TRIDENT, Items.BOW, Items.CROSSBOW, Items.ELYTRA};

}
