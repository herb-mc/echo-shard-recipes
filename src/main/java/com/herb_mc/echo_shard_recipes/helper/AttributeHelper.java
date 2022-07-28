package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.*;

public class AttributeHelper {

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

        public AttributeItem(Item i, String s, ItemChecker ic, Formatting t) {
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

        public AttributeItem(Item i, String s, ItemChecker ic, Formatting t, PostProcess p, IngredientProcessor in) {
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

        public AttributeItem(Item i, String s, ItemChecker ic, Formatting t, String st, EntityAttribute e, String str, EntityAttributeModifier.Operation o, double d) {
            this(i, s, ic, t, NONE, NO_REQ);
            uuid = UUID.fromString(st);
            attribute = e;
            tag = MOD_ID + "." + str;
            op = o;
            base = d;
        }

    }

    public interface ItemChecker {
        boolean isValidItem(Item i);
    }

    public interface PostProcess {
        void process(ItemStack i);
    }

    public interface IngredientProcessor {
        void process(ItemStack in, ItemStack i);
    }

    public static final String HAS_ATTRIBUTE = "HasShardAttribute";
    public static final String ATTRIBUTE = "ShardAttribute";
    public static final String STORED_ATTRIBUTE = "StoredShardAttribute";
    public static final ItemChecker TRUE_MELEE = (i) -> (i instanceof AxeItem || i instanceof SwordItem);
    public static final ItemChecker MELEE = (i) -> (TRUE_MELEE.isValidItem(i) || i instanceof TridentItem);
    public static final ItemChecker TRUE_RANGED = (i) -> (i instanceof BowItem || i instanceof CrossbowItem);
    public static final ItemChecker RANGED = (i) -> (TRUE_RANGED.isValidItem(i) || i instanceof TridentItem);
    public static final ItemChecker WEAPON = (i) -> (MELEE.isValidItem(i) || RANGED.isValidItem(i));
    public static final ItemChecker RANGED_AND_ROD = (i) -> (RANGED.isValidItem(i) || i instanceof FishingRodItem);
    public static final ItemChecker BOW = (i) -> (i instanceof BowItem);
    public static final ItemChecker ROD = (i) -> (i instanceof FishingRodItem);
    public static final ItemChecker TRUE_TOOL = (i) -> (i instanceof HoeItem || i instanceof PickaxeItem || i instanceof ShovelItem);
    public static final ItemChecker TOOL = (i) -> (i instanceof AxeItem || i instanceof HoeItem || i instanceof PickaxeItem || i instanceof ShovelItem);
    public static final ItemChecker ARMOR = (i) -> (i instanceof ArmorItem || i instanceof ElytraItem);
    public static final ItemChecker SHIELD = (i) -> (i instanceof ShieldItem);
    public static final ItemChecker MISC = (i) -> (i instanceof ShearsItem || i instanceof FishingRodItem || i instanceof ShieldItem || i instanceof FlintAndSteelItem || i instanceof OnAStickItem<?>);
    public static final ItemChecker EQUIPS = (i) -> (WEAPON.isValidItem(i) || TOOL.isValidItem(i) || ARMOR.isValidItem(i) || MISC.isValidItem(i));
    public static final ItemChecker FLINT_AND_STEEL = (i) -> (i instanceof FlintAndSteelItem);
    public static final ItemChecker HOE = (i) -> (i instanceof HoeItem);
    public static final ItemChecker HEAD = (i) -> (i instanceof ArmorItem && ((ArmorItem) i).getSlotType() == EquipmentSlot.HEAD);
    public static final ItemChecker CHEST = (i) -> (i instanceof ArmorItem && ((ArmorItem) i).getSlotType() == EquipmentSlot.CHEST);
    public static final ItemChecker CHEST_EXT = (i) -> (i instanceof ElytraItem || CHEST.isValidItem(i));
    public static final ItemChecker LEGS = (i) -> (i instanceof ArmorItem && ((ArmorItem) i).getSlotType() == EquipmentSlot.LEGS);
    public static final ItemChecker FEET = (i) -> (i instanceof ArmorItem && ((ArmorItem) i).getSlotType() == EquipmentSlot.FEET);
    public static final ItemChecker FISH = (i) -> (i == Items.SALMON || i == Items.COD || i == Items.TROPICAL_FISH);
    public static final ItemChecker SPELL_FOCUS = (i) -> (i == Items.EMERALD);
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
    public static final IngredientProcessor SET_SPELL_FOCUS = (in, i) -> {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("spell_focus", true);
        i.setNbt(nbt);
        i.setCustomName(MutableText.of(Text.of("Spell Focus").getContent()).setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.DARK_PURPLE)));
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
        ATTRIBUTE_ITEMS.put("adept", new AttributeItem(Items.BOOKSHELF, "Adept", ARMOR, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("attuned", new AttributeItem(Items.EXPERIENCE_BOTTLE, "Attuned", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("energized", new AttributeItem(Items.REDSTONE_BLOCK, "Energized", ARMOR, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("excavator", new AttributeItem(Items.STONE, "Excavator", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("hasty", new AttributeItem(Items.SUGAR, "Hasty", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("magnetized", new AttributeItem(Items.RAW_IRON, "Magnetized", TOOL, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("faster_reel", new AttributeItem(Items.PRISMARINE_CRYSTALS, "Faster Reel", ROD, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("high_test", new AttributeItem(Items.STRING, "High Test", ROD, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("power_assist", new AttributeItem(Items.REDSTONE, "Power Assist", CHEST_EXT, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("scholarly", new AttributeItem(Items.LECTERN, "Scholarly", ARMOR, Formatting.AQUA));
        ATTRIBUTE_ITEMS.put("terraforming", new AttributeItem(Items.GRASS_BLOCK, "Terraforming", TOOL, Formatting.AQUA, "36545877-4a33-4614-a0fa-95d765ca5416", EntityAttributes.GENERIC_ATTACK_SPEED, "terraforming", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, -0.8));
        ATTRIBUTE_ITEMS.put("anti_corrosive", new AttributeItem(Items.GOLD_INGOT, "Anti-Corrosive", CHEST_EXT, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("indomitable", new AttributeItem(Items.SCUTE, "Indomitable", SHIELD, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("reflecting", new AttributeItem(Items.SHIELD, "Reflecting", SHIELD, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("reflex", new AttributeItem(Items.FEATHER, "Reflex", LEGS, Formatting.GRAY, "8b2ce124-8c51-4949-bc26-ac1719576fa7", EntityAttributes.GENERIC_MOVEMENT_SPEED, "reflex", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, 0.3));
        ATTRIBUTE_ITEMS.put("reinforced", new AttributeItem(Items.ANVIL, "Reinforced", ARMOR, Formatting.GRAY, "8b2ce124-8c71-4949-bca6-ba1779662fa7", EntityAttributes.GENERIC_ARMOR, "reinforced", EntityAttributeModifier.Operation.ADDITION, 1.0));
        ATTRIBUTE_ITEMS.put("rejuvenating", new AttributeItem(Items.GOLDEN_APPLE, "Rejuvenating", ARMOR, Formatting.GRAY, "06790794-1df4-4ee9-b4c8-0f9842f6ac54", EntityAttributes.GENERIC_MAX_HEALTH, "rejuvenating", EntityAttributeModifier.Operation.ADDITION, 1.0));
        ATTRIBUTE_ITEMS.put("resilient", new AttributeItem(Items.LEATHER, "Resilient", ARMOR, Formatting.GRAY, "b76e3bac-e417-4a8c-8ed3-1d843adf311e", EntityAttributes.GENERIC_ARMOR_TOUGHNESS, "resilient", EntityAttributeModifier.Operation.ADDITION, 2.0));
        ATTRIBUTE_ITEMS.put("revenge", new AttributeItem(Items.IRON_SWORD, "Revenge", SHIELD, Formatting.GRAY));
        ATTRIBUTE_ITEMS.put("stalwart", new AttributeItem(Items.IRON_BLOCK, "Stalwart", ARMOR, Formatting.GRAY, "ed25083c-c160-4522-b8ab-cec6287370b0", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "stalwart", EntityAttributeModifier.Operation.ADDITION, 0.1));
        ATTRIBUTE_ITEMS.put("steady_body", new AttributeItem(Items.COBBLED_DEEPSLATE, "Steady Body", LEGS, Formatting.GRAY, "7dd3b0f6-a0d7-4de1-918c-3df131b1e53c", EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "steady_body", EntityAttributeModifier.Operation.ADDITION, 1.0));
        ATTRIBUTE_ITEMS.put("turtle_shell", new AttributeItem(Items.TURTLE_HELMET, "Turtle Shell", CHEST_EXT, Formatting.GRAY));
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
        ATTRIBUTE_ITEMS.put("archmage", new AttributeItem(Items.ENCHANTING_TABLE, "Archmage", CHEST_EXT, Formatting.LIGHT_PURPLE, UPGRADE_PROT, NO_REQ));
        ATTRIBUTE_ITEMS.put("crushing_wave", new AttributeItem(Items.SPONGE, "Crushing Wave", FISH, Formatting.LIGHT_PURPLE, ENCHANT_GLINT, NO_REQ));
        ATTRIBUTE_ITEMS.put("gun_ho", new AttributeItem(Items.TNT, "Gun Ho", HOE, Formatting.LIGHT_PURPLE, "7a11103b-8823-4db9-bf48-ce4801a3ec57", EntityAttributes.GENERIC_ATTACK_SPEED, "gun_hoe", EntityAttributeModifier.Operation.MULTIPLY_TOTAL, 0));
        ATTRIBUTE_ITEMS.put("flowing_water", new AttributeItem(Items.TRIDENT, "Flowing Water", FISH, Formatting.LIGHT_PURPLE, ENCHANT_GLINT, NO_REQ));
        ATTRIBUTE_ITEMS.put("infernal", new AttributeItem(Items.BEACON, "Infernal", CHEST_EXT, Formatting.LIGHT_PURPLE, NONE, NO_REQ));
        ATTRIBUTE_ITEMS.put("rip_current", new AttributeItem(Items.HEART_OF_THE_SEA, "Rip Current", FISH, Formatting.LIGHT_PURPLE, CRUSHING_WAVE, NO_REQ));
        ATTRIBUTE_ITEMS.put("spell_focus", new AttributeItem(Items.BOOK, "Spell Focus", SPELL_FOCUS, Formatting.LIGHT_PURPLE, NONE, SET_SPELL_FOCUS));
        ATTRIBUTE_ITEMS.put("voided", new AttributeItem(Items.ELYTRA, "Voided", CHEST_EXT, Formatting.LIGHT_PURPLE, UPGRADE_PROT, NO_REQ));
    }

    public static String getAttribute(ItemStack i) {
        return (i.getNbt() != null) ? i.getNbt().getString(ATTRIBUTE) : "";
    }

    public static void removeAttribute(LivingEntity entity, EntityAttribute attribute, UUID uuid){
        EntityAttributeInstance instance = entity.getAttributeInstance(attribute);
        if (instance != null && instance.getModifier(uuid) != null)
            instance.removeModifier(uuid);
    }

    public static void addAttribute(LivingEntity e, AttributeItem i) {
        if (i != null) {
            EntityAttributeInstance instance = e.getAttributeInstance(i.attribute);
            if (instance != null && i.uuid != null) instance.addTemporaryModifier(new EntityAttributeModifier(i.uuid, i.tag, i.base, i.op));
        }
    }

    public static void addAttribute(LivingEntity e, AttributeItem i, double base) {
        EntityAttributeInstance instance = e.getAttributeInstance(i.attribute);
        if (instance != null && i.uuid != null)
            instance.addTemporaryModifier(new EntityAttributeModifier(i.uuid, i.tag, base, i.op));
    }

    public static void addAttribute(LivingEntity l, EntityAttribute e, UUID uuid, String tag, double base, EntityAttributeModifier.Operation op) {
        EntityAttributeInstance instance = l.getAttributeInstance(e);
        if (instance != null && uuid != null)
            instance.addTemporaryModifier(new EntityAttributeModifier(uuid, tag, base, op));
    }

}
