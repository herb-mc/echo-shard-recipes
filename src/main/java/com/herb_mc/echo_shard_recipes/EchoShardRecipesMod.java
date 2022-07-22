package com.herb_mc.echo_shard_recipes;

import com.herb_mc.echo_shard_recipes.helper.Spells;
import com.herb_mc.echo_shard_recipes.recipes.EchoAugmentRecipe;
import com.herb_mc.echo_shard_recipes.recipes.EchoShardBaseRecipe;
import com.herb_mc.echo_shard_recipes.recipes.EchoWandRecipe;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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

        AttributeItem(Item i, String s, ItemChecker ic, Formatting t, PostProcess p, IngredientProcessor in, String st, EntityAttribute e, String str, EntityAttributeModifier.Operation o, double d) {
            this(i, s, ic, t, p, in);
            uuid = UUID.fromString(st);
            attribute = e;
            tag = MOD_ID + "." + str;
            op = o;
            base = d;
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
    public static final SpecialRecipeSerializer<EchoWandRecipe> ECHO_WAND = new SpecialRecipeSerializer<>(EchoWandRecipe::new);
    public static final Random ECHO_SHARD_RANDOM = new Random();

    public static final String HAS_PARTICLE = "HasShardParticleEffect";
    public static final String PARTICLE = "ShardParticleEffect";
    public static final String HAS_ATTRIBUTE = "HasShardAttribute";
    public static final String ATTRIBUTE = "ShardAttribute";
    public static final String STORED_ATTRIBUTE = "StoredShardAttribute";
    public static final String SPELL = "StoredSpell";

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
    public static HashMap<String, Spells.WandItem> WANDS = new HashMap<>();

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
        Registry.register(Registry.RECIPE_SERIALIZER, new Identifier(MOD_ID, "crafting_special_echo_wand"), ECHO_WAND);
    }
}
