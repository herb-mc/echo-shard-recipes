package com.herb_mc.echo_shard_recipes;

import com.herb_mc.echo_shard_recipes.recipes.EchoAugmentRecipe;
import com.herb_mc.echo_shard_recipes.recipes.EchoShardBaseRecipe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class EchoShardRecipesMod {

    public interface ItemChecker {
        boolean isValidItem(Item i);
    }

    public interface PostProcess {
        void process(ItemStack i);
    }

    public interface IngredientProcessor {
        void process(ItemStack in, ItemStack i);
    }

    public static class AttributeItem {

        public Item item;
        public String string;
        public Formatting color;
        public ItemChecker itemChecker;
        public UUID uuid;
        public EntityAttribute attribute;
        public String tag;
        public EntityAttributeModifier.Operation op;
        public PostProcess processor;
        public IngredientProcessor ingredientProcessor;
        public double base;

        AttributeItem(Item i, String s, ItemChecker ic, Formatting t) {
            item = i;
            string = s;
            color = t;
            itemChecker = ic;
            processor = NONE;
            ingredientProcessor = NO_REQ;
            uuid = null;
            attribute = null;
            tag = null;
            op = null;
            base = 0.0;
        }

        AttributeItem(Item i, String s, ItemChecker ic, Formatting t, PostProcess p, IngredientProcessor in) {
            item = i;
            string = s;
            color = t;
            itemChecker = ic;
            processor = p;
            ingredientProcessor = in;
            uuid = null;
            attribute = null;
            tag = null;
            op = null;
            base = 0.0;
        }

        AttributeItem(Item i, String s, ItemChecker ic, Formatting t, PostProcess p, IngredientProcessor in, String st, EntityAttribute e, String str, EntityAttributeModifier.Operation o, double d) {
            this(i, s, ic, t, p, in);
            uuid = UUID.fromString(st);
            attribute = e;
            tag = MOD_ID + "." + str;
            op = o;
            base = d;
        }

        AttributeItem(Item i, String s, ItemChecker ic, Formatting t, String st, EntityAttribute e, String str, EntityAttributeModifier.Operation o, double d) {
            this(i, s, ic, t, NONE, NO_REQ);
            uuid = UUID.fromString(st);
            attribute = e;
            tag = MOD_ID + "." + str;
            op = o;
            base = d;
        }

    }

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

    public static final String MOD_ID = "echo_shard_recipes";
    public static final Logger LOGGER = LoggerFactory.getLogger("echo_shard_recipes");
    public static final SpecialRecipeSerializer<EchoShardBaseRecipe> ECHO_SHARD_BASE = new SpecialRecipeSerializer<>(EchoShardBaseRecipe::new);
    public static final SpecialRecipeSerializer<EchoAugmentRecipe> ECHO_SHARD_AUGMENT = new SpecialRecipeSerializer<>(EchoAugmentRecipe::new);
    public static final Random ECHO_SHARD_RANDOM = new Random();

    public static final String HAS_PARTICLE = "HasShardParticleEffect";
    public static final String PARTICLE = "ShardParticleEffect";
    public static final String HAS_ATTRIBUTE = "HasShardAttribute";
    public static final String ATTRIBUTE = "ShardAttribute";
    public static final String STORED_ATTRIBUTE = "StoredShardAttribute";

    public static final ItemChecker TRUE_MELEE = (i) -> (i instanceof AxeItem || i instanceof SwordItem);
    public static final ItemChecker MELEE = (i) -> (TRUE_MELEE.isValidItem(i) || i instanceof TridentItem);
    public static final ItemChecker TRUE_RANGED = (i) -> (i instanceof BowItem || i instanceof CrossbowItem);
    public static final ItemChecker BOW = (i) -> (i instanceof BowItem);
    public static final ItemChecker RANGED = (i) -> (TRUE_RANGED.isValidItem(i) || i instanceof TridentItem);
    public static final ItemChecker RANGED_AND_ROD = (i) -> (RANGED.isValidItem(i) || i instanceof FishingRodItem);
    public static final ItemChecker ROD = (i) -> (i instanceof FishingRodItem);
    public static final ItemChecker WEAPON = (i) -> (MELEE.isValidItem(i) || RANGED.isValidItem(i));
    public static final ItemChecker TRUE_TOOL = (i) -> (i instanceof HoeItem || i instanceof PickaxeItem || i instanceof ShovelItem);
    public static final ItemChecker TOOL = (i) -> (i instanceof AxeItem || i instanceof HoeItem || i instanceof PickaxeItem || i instanceof ShovelItem);
    public static final ItemChecker ARMOR = (i) -> (i instanceof ArmorItem || i instanceof ElytraItem);
    public static final ItemChecker SHIELD = (i) -> (i instanceof ShieldItem);
    public static final ItemChecker MISC = (i) -> (i instanceof ShearsItem || i instanceof FishingRodItem || i instanceof ShieldItem || i instanceof FlintAndSteelItem || i instanceof OnAStickItem<?>);
    public static final ItemChecker FLINT_AND_STEEL = (i) -> (i instanceof FlintAndSteelItem);
    public static final ItemChecker HOE = (i) -> (i instanceof HoeItem);
    public static final ItemChecker HEAD = (i) -> (i instanceof ArmorItem && ((ArmorItem) i).getSlotType() == EquipmentSlot.HEAD);
    public static final ItemChecker CHEST = (i) -> (i instanceof ArmorItem && ((ArmorItem) i).getSlotType() == EquipmentSlot.CHEST);
    public static final ItemChecker LEGS = (i) -> (i instanceof ArmorItem && ((ArmorItem) i).getSlotType() == EquipmentSlot.LEGS);
    public static final ItemChecker FEET = (i) -> (i instanceof ArmorItem && ((ArmorItem) i).getSlotType() == EquipmentSlot.FEET);
    public static final ItemChecker FISH = (i) -> (i == Items.SALMON || i == Items.COD || i == Items.TROPICAL_FISH);
    public static final ItemChecker EQUIPS = (i) -> (WEAPON.isValidItem(i) || TOOL.isValidItem(i) || ARMOR.isValidItem(i) || MISC.isValidItem(i));
    public static final ItemChecker ANY = (i) -> true;

    public static final PostProcess NONE = (i) -> {};
    public static final PostProcess CRUSHING_WAVE = (i) -> {i.addEnchantment(Enchantments.KNOCKBACK, 2); i.addEnchantment(Enchantments.SMITE, 1); i.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);};
    public static final PostProcess ENCHANT_GLINT = (i) -> {i.addEnchantment(Enchantments.PUNCH, 1); i.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);};
    public static final PostProcess UPGRADE_ENCHANTS = (i) -> {
        Map<Enchantment, Integer> m = EnchantmentHelper.get(i);
        if (m.entrySet().size() == 0) i.setCount(0);
        else for (Map.Entry<Enchantment, Integer> entry : m.entrySet()) if (entry.getKey().getMaxLevel() != 1) entry.setValue(entry.getValue() + 1);
        EnchantmentHelper.set(m, i);
    };
    public static final PostProcess UPGRADE_PROT = (i) -> {
        Map<Enchantment, Integer> m = EnchantmentHelper.get(i);
        for (Map.Entry<Enchantment, Integer> entry : m.entrySet()) if (entry.getKey() == Enchantments.PROTECTION) entry.setValue(entry.getValue() + 1);
        EnchantmentHelper.set(m, i);
    };
    public static final IngredientProcessor NO_REQ = (in, i) -> {};
    public static final IngredientProcessor REQUIRES_BINDING = (in, i) -> {
        boolean remove = true;
        Identifier id = EnchantmentHelper.getEnchantmentId(Enchantments.BINDING_CURSE);
        NbtList nbt = EnchantedBookItem.getEnchantmentNbt(in);
        for(int j = 0; j < nbt.size(); ++j) {
            NbtCompound nbtCompound = nbt.getCompound(j);
            Identifier identifier2 = EnchantmentHelper.getIdFromNbt(nbtCompound);
            if (identifier2 != null && identifier2.equals(id)) remove = false;
        }
        if (remove) i.setCount(0);
    };

    public static HashMap<String, AttributeItem> ATTRIBUTE_ITEMS = new HashMap<>();

    static {
        ATTRIBUTE_ITEMS.put("alchemist", new AttributeItem(Items.POTION, "Alchemist", MELEE, Formatting.RED, "ac18a4c5-ffff-4777-8827-b62582306fe3", EntityAttributes.GENERIC_ATTACK_DAMAGE, "alchemist", EntityAttributeModifier.Operation.ADDITION, 1.0));
        ATTRIBUTE_ITEMS.put("aquadynamic", new AttributeItem(Items.CONDUIT, "Aquadynamic", BOW, Formatting.RED));
        ATTRIBUTE_ITEMS.put("buckshot", new AttributeItem(Items.IRON_NUGGET, "Buckshot", TRUE_RANGED, Formatting.RED));
        ATTRIBUTE_ITEMS.put("fireball", new AttributeItem(Items.GHAST_TEAR, "Fireball", FLINT_AND_STEEL, Formatting.RED));
        ATTRIBUTE_ITEMS.put("firebrand", new AttributeItem(Items.LAVA_BUCKET, "Firebrand", MELEE, Formatting.RED));
        ATTRIBUTE_ITEMS.put("flamethrower", new AttributeItem(Items.FIRE_CHARGE, "Flamethrower", FLINT_AND_STEEL, Formatting.RED));
        ATTRIBUTE_ITEMS.put("hitscan", new AttributeItem(Items.ENDER_PEARL, "Hitscan", BOW, Formatting.RED));
        ATTRIBUTE_ITEMS.put("light", new AttributeItem(Items.DIAMOND, "Light", TRUE_MELEE, Formatting.RED, "ac18a4c5-c926-4777-8827-b62582306fe3", EntityAttributes.GENERIC_ATTACK_SPEED, "light", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, 0.2));
        ATTRIBUTE_ITEMS.put("metaphysical", new AttributeItem(Items.ENDER_EYE, "Metaphysical", TRUE_RANGED, Formatting.RED));
        ATTRIBUTE_ITEMS.put("momentum", new AttributeItem(Items.LAPIS_LAZULI, "Momentum", MELEE, Formatting.RED));
        ATTRIBUTE_ITEMS.put("jagged", new AttributeItem(Items.PRISMARINE_SHARD, "Jagged", RANGED_AND_ROD, Formatting.RED));
        ATTRIBUTE_ITEMS.put("sharpened", new AttributeItem(Items.FLINT, "Sharpened", MELEE, Formatting.RED, "61930a5a-af4e-46dd-8ca0-22bcabbee462", EntityAttributes.GENERIC_ATTACK_DAMAGE, "sharpened", EntityAttributeModifier.Operation.ADDITION, 3.0));
        ATTRIBUTE_ITEMS.put("sharpshooter", new AttributeItem(Items.SPYGLASS, "Sharp Shot", HEAD, Formatting.RED));
        ATTRIBUTE_ITEMS.put("snipe_shot", new AttributeItem(Items.ARROW, "Snipe Shot", HEAD, Formatting.RED, "36545877-4a33-4614-a0fa-95d768ba5316", EntityAttributes.GENERIC_ATTACK_DAMAGE, "snipe_shot", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, -0.3));
        ATTRIBUTE_ITEMS.put("super_luck", new AttributeItem(Items.EMERALD, "Super Luck", BOW, Formatting.RED));
        ATTRIBUTE_ITEMS.put("superphysical", new AttributeItem(Items.END_ROD, "Superphysical", BOW, Formatting.RED));
        ATTRIBUTE_ITEMS.put("stonebreaker", new AttributeItem(Items.STONECUTTER, "Stonebreaker", TRUE_TOOL, Formatting.RED, "36545877-4a33-4614-a0fa-95d768ba5416", EntityAttributes.GENERIC_ATTACK_SPEED, "stonebreaker", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, -0.3));
        ATTRIBUTE_ITEMS.put("antigravity", new AttributeItem(Items.WARPED_FUNGUS, "Antigravity", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("attuned", new AttributeItem(Items.EXPERIENCE_BOTTLE, "Attuned", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("excavator", new AttributeItem(Items.STONE, "Excavator", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("glowing", new AttributeItem(Items.GLOW_INK_SAC, "Glowing", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("hasty", new AttributeItem(Items.SUGAR, "Hasty", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("magnetized", new AttributeItem(Items.RAW_IRON, "Magnetized", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("faster_reel", new AttributeItem(Items.PRISMARINE_CRYSTALS, "Faster Reel", ROD, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("high_test", new AttributeItem(Items.STRING, "High Test", ROD, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("power_assist", new AttributeItem(Items.REDSTONE, "Power Assist", CHEST, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("terraforming", new AttributeItem(Items.GRASS_BLOCK, "Terraforming", TOOL, Formatting.AQUA, "36545877-4a33-4614-a0fa-95d765ca5416", EntityAttributes.GENERIC_ATTACK_SPEED, "terraforming", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, -0.8));
        ATTRIBUTE_ITEMS.put("anti_corrosive", new AttributeItem(Items.GOLD_INGOT, "Anti-Corrosive", CHEST, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("indomitable", new AttributeItem(Items.SCUTE, "Indomitable", SHIELD, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("reflecting", new AttributeItem(Items.SHIELD, "Reflecting", SHIELD, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("reflex", new AttributeItem(Items.FEATHER, "Reflex", LEGS, Formatting.GRAY, "8b2ce124-8c51-4949-bc26-ac1719576fa7", EntityAttributes.GENERIC_MOVEMENT_SPEED, "reflex", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, 0.3));
        ATTRIBUTE_ITEMS.put("reinforced", new AttributeItem(Items.ANVIL, "Reinforced", ARMOR, Formatting.GRAY, "8b2ce124-8c71-4949-bca6-ba1779662fa7", EntityAttributes.GENERIC_ARMOR, "reinforced", EntityAttributeModifier.Operation.ADDITION, 1.0));
        ATTRIBUTE_ITEMS.put("rejuvenating", new AttributeItem(Items.GOLDEN_APPLE, "Rejuvenating", ARMOR, Formatting.GRAY, "06790794-1df4-4ee9-b4c8-0f9842f6ac54", EntityAttributes.GENERIC_MAX_HEALTH, "rejuvenating", EntityAttributeModifier.Operation.ADDITION, 1.0));
        ATTRIBUTE_ITEMS.put("resilient", new AttributeItem(Items.LEATHER, "Resilient", ARMOR, Formatting.GRAY, "b76e3bac-e417-4a8c-8ed3-1d843adf311e", EntityAttributes.GENERIC_ARMOR_TOUGHNESS, "resilient", EntityAttributeModifier.Operation.ADDITION, 2.0));
        ATTRIBUTE_ITEMS.put("revenge", new AttributeItem(Items.IRON_SWORD, "Revenge", SHIELD, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("stalwart", new AttributeItem(Items.IRON_BLOCK, "Stalwart", ARMOR, Formatting.GRAY, "ed25083c-c160-4522-b8ab-cec6287370b0", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "stalwart", EntityAttributeModifier.Operation.ADDITION, 0.1));
        ATTRIBUTE_ITEMS.put("steady_body", new AttributeItem(Items.COBBLED_DEEPSLATE, "Steady Body", LEGS, Formatting.GRAY, "7dd3b0f6-a0d7-4de1-918c-3df131b1e53c", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "steady_body", EntityAttributeModifier.Operation.ADDITION, 1.0));
        ATTRIBUTE_ITEMS.put("turtle_shell", new AttributeItem(Items.TURTLE_HELMET, "Turtle Shell", CHEST, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("featherweight", new AttributeItem(Items.PHANTOM_MEMBRANE, "Featherweight", FEET, Formatting.GREEN));
        ATTRIBUTE_ITEMS.put("grappling", new AttributeItem(Items.FISHING_ROD, "Grappling", ROD, Formatting.GREEN));
        ATTRIBUTE_ITEMS.put("heat_conductor", new AttributeItem(Items.EMERALD, "Heat Conductor", LEGS, Formatting.GREEN, "bafb4ce9-c1ee-4247-9570-dcc412335e5b", EntityAttributes.GENERIC_MOVEMENT_SPEED, "heat_conductor", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, 0.15));
        ATTRIBUTE_ITEMS.put("levitator", new AttributeItem(Items.SHULKER_SHELL, "Levitator", FEET, Formatting.GREEN));
        ATTRIBUTE_ITEMS.put("machine_assist", new AttributeItem(Items.PISTON, "Machine Assist", LEGS, Formatting.GREEN));
        ATTRIBUTE_ITEMS.put("swift", new AttributeItem(Items.EMERALD, "Swift", ARMOR, Formatting.GREEN, "b4fa00c0-ac70-423a-9efa-ae86fb46be8f", EntityAttributes.GENERIC_MOVEMENT_SPEED, "swift", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, 0.04));
        ATTRIBUTE_ITEMS.put("enhanced", new AttributeItem(Items.NETHERITE_INGOT, "Enhanced", ANY, Formatting.YELLOW, UPGRADE_ENCHANTS, NO_REQ));
        ATTRIBUTE_ITEMS.put("fireproof", new AttributeItem(Items.BLAZE_ROD, "Fireproof", EQUIPS, Formatting.YELLOW));
        ATTRIBUTE_ITEMS.put("unbreakable", new AttributeItem(Items.OBSIDIAN, "Unbreakable", EQUIPS, Formatting.YELLOW));
        ATTRIBUTE_ITEMS.put("soulbound", new AttributeItem(Items.ENCHANTED_BOOK, "Soulbound", EQUIPS, Formatting.YELLOW, NONE, REQUIRES_BINDING));
        ATTRIBUTE_ITEMS.put("crushing_wave", new AttributeItem(Items.SPONGE, "Crushing Wave", FISH, Formatting.LIGHT_PURPLE, ENCHANT_GLINT, NO_REQ));
        ATTRIBUTE_ITEMS.put("gun_ho", new AttributeItem(Items.TNT, "Gun Ho", HOE, Formatting.LIGHT_PURPLE, "7a11103b-8823-4db9-bf48-ce4801a3ec57", EntityAttributes.GENERIC_ATTACK_SPEED, "gun_hoe", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, 0));
        ATTRIBUTE_ITEMS.put("flowing_water", new AttributeItem(Items.TRIDENT, "Flowing Water", FISH, Formatting.LIGHT_PURPLE, ENCHANT_GLINT, NO_REQ));
        ATTRIBUTE_ITEMS.put("infernal", new AttributeItem(Items.BEACON, "Infernal", CHEST, Formatting.LIGHT_PURPLE, NONE, NO_REQ));
        ATTRIBUTE_ITEMS.put("rip_current", new AttributeItem(Items.HEART_OF_THE_SEA, "Rip Current", FISH, Formatting.LIGHT_PURPLE, CRUSHING_WAVE, NO_REQ));
        ATTRIBUTE_ITEMS.put("voided", new AttributeItem(Items.ELYTRA, "Voided", CHEST, Formatting.LIGHT_PURPLE, UPGRADE_PROT, NO_REQ));
    }

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

    public static void init() {
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "crafting_special_echo_augments"), ECHO_SHARD_AUGMENT);
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "crafting_special_echo_shard_particle_base"), ECHO_SHARD_BASE);
    }
}
