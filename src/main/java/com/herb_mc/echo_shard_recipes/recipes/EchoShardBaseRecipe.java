package com.herb_mc.echo_shard_recipes.recipes;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Map;

public class EchoShardBaseRecipe extends SpecialCraftingRecipe {

    public EchoShardBaseRecipe(Identifier identifier) {
        super(identifier);
    }

    private int containsParticle(ItemStack i) {
        int id = 0;
        for (EchoShardRecipesMod.ParticleItem t : EchoShardRecipesMod.PARTICLE_ITEMS) {
            if (i.isOf(t.item.asItem())) return id;
            id++;
        }
        return -1;
    }

    private String containsAttribute(ItemStack i) {
        for (Map.Entry<String, EchoShardRecipesMod.AttributeItem> t : EchoShardRecipesMod.ATTRIBUTE_ITEMS.entrySet())
            if (i.isOf(t.getValue().item.asItem())) return t.getKey();
        return null;
    }

    private boolean isEmptyShard(ItemStack in) {
        return !in.getOrCreateNbt().getBoolean(EchoShardRecipesMod.HAS_PARTICLE);
    }

    private MutableText getText(String type, String value, Formatting f) {
        return MutableText.of(Text.of(type + ": [").getContent()).setStyle(Style.EMPTY.withFormatting(Formatting.WHITE).withItalic(false).withBold(false))
            .append(MutableText.of(Text.of(value).getContent()).setStyle(Style.EMPTY.withFormatting(f).withItalic(false).withBold(false)))
            .append(MutableText.of(Text.of("]").getContent()).setStyle(Style.EMPTY.withFormatting(Formatting.WHITE).withItalic(false).withBold(false)));
    }

    public void addParticles(ItemStack output, int id) {
        NbtCompound nbt = output.getOrCreateNbt();
        nbt.putBoolean(EchoShardRecipesMod.HAS_PARTICLE, true);
        nbt.putInt(EchoShardRecipesMod.PARTICLE, id);
        NbtCompound nbtDisplay = nbt.getCompound(ItemStack.DISPLAY_KEY);
        NbtList nbtLore = (NbtList) nbtDisplay.get(ItemStack.LORE_KEY);
        if (nbtLore == null) nbtLore = new NbtList();
        nbtLore.add(NbtString.of(Text.Serializer.toJson(
                getText("Particle", EchoShardRecipesMod.PARTICLE_ITEMS[id].string, EchoShardRecipesMod.PARTICLE_ITEMS[id].color)
        )));
        nbtDisplay.put(ItemStack.LORE_KEY, nbtLore);
        nbt.put(ItemStack.DISPLAY_KEY, nbtDisplay);
        output.setNbt(nbt);
    }

    public void addAttributes(ItemStack output, String id) {
        NbtCompound nbt = output.getOrCreateNbt();
        nbt.putBoolean(EchoShardRecipesMod.HAS_ATTRIBUTE, true);
        nbt.putString(EchoShardRecipesMod.ATTRIBUTE, id);
        NbtCompound nbtDisplay = nbt.getCompound(ItemStack.DISPLAY_KEY);
        NbtList nbtLore = (NbtList) nbtDisplay.get(ItemStack.LORE_KEY);
        if (nbtLore == null) nbtLore = new NbtList();
        nbtLore.add(NbtString.of(Text.Serializer.toJson(
                getText("Attribute", EchoShardRecipesMod.ATTRIBUTE_ITEMS.get(id).string, EchoShardRecipesMod.ATTRIBUTE_ITEMS.get(id).color)
        )));
        nbtDisplay.put(ItemStack.LORE_KEY, nbtLore);
        nbt.put(ItemStack.DISPLAY_KEY, nbtDisplay);
        output.setNbt(nbt);
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        boolean has_echo_shard = false;
        boolean has_valid_particle = false;
        boolean has_gunpowder = false;
        boolean has_valid_attribute = false;
        boolean has_nether_star = false;
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty())
                if (itemStack.isOf(Items.ECHO_SHARD.asItem()) && isEmptyShard(itemStack) && !has_echo_shard) has_echo_shard = true;
                else if (itemStack.isOf(Items.GUNPOWDER) && !has_gunpowder) has_gunpowder = true;
                else if (itemStack.isOf(Items.NETHER_STAR) && !has_nether_star) has_nether_star = true;
                else if (containsParticle(itemStack) != -1 || containsAttribute(itemStack) != null);
                else return false;
        }
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty())
                if (itemStack.isOf(Items.GUNPOWDER) || itemStack.isOf(Items.NETHER_STAR) || itemStack.isOf(Items.ECHO_SHARD));
                else if (has_gunpowder && containsParticle(itemStack) != -1 && !has_valid_particle) has_valid_particle = true;
                else if (has_nether_star && containsAttribute(itemStack) != null && !has_valid_attribute) has_valid_attribute = true;
                else return false;
        }
        return has_echo_shard && (has_valid_particle || has_valid_attribute) && (has_valid_particle == has_gunpowder && has_nether_star == has_valid_attribute);
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        int particle = -1;
        String attribute = null;
        boolean has_gunpowder = false;
        boolean has_nether_star = false;
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty())
                if (itemStack.isOf(Items.GUNPOWDER) && !has_gunpowder) has_gunpowder = true;
                else if (itemStack.isOf(Items.NETHER_STAR) && !has_nether_star) has_nether_star = true;
        }
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack temp = inventory.getStack(i);
            if (has_gunpowder && particle == -1) particle = containsParticle(temp);
            else if (has_nether_star && attribute == null) attribute = containsAttribute(temp);
        }
        ItemStack output = new ItemStack(Items.ECHO_SHARD, 1);
        if (particle != -1) addParticles(output, particle);
        if (attribute != null) addAttributes(output, attribute);
        output.setCustomName(MutableText.of(Text.of("Augment Shard").getContent()).setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.AQUA)));
        output.addEnchantment(Enchantments.PIERCING, 1);
        output.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        return output;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EchoShardRecipesMod.ECHO_SHARD_BASE;
    }

}
