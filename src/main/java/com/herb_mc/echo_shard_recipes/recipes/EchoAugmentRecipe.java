package com.herb_mc.echo_shard_recipes.recipes;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class EchoAugmentRecipe extends SpecialCraftingRecipe {

    public EchoAugmentRecipe(Identifier id) {
        super(id);
    }

    private boolean hasParticleEffect(ItemStack in) {
        return in.getNbt() != null && (in.getNbt().getBoolean(EchoShardRecipesMod.HAS_PARTICLE));
    }

    private boolean hasAttribute(ItemStack in) {
        return in.getNbt() != null && (in.getNbt().getBoolean(EchoShardRecipesMod.HAS_ATTRIBUTE));
    }

    private String getShardAttribute(ItemStack in) {
        assert in.getNbt() != null;
        return in.getNbt().getString(EchoShardRecipesMod.STORED_ATTRIBUTE).equals("") ? null : in.getNbt().getString(EchoShardRecipesMod.STORED_ATTRIBUTE);
    }

    private boolean hasValidParticleItem(ItemStack i) {
        for (Item t : EchoShardRecipesMod.validParticleItems)
            if (i.isOf(t.asItem()) && i.getNbt() != null && !hasParticleEffect(i)) return true;
        return false;
    }

    private boolean hasValidAttributedItem(ItemStack i, String augment) {
        return !augment.equals("") && EchoShardRecipesMod.ATTRIBUTE_ITEMS.get(augment).itemChecker.isValidItem(i.getItem()) && !hasAttribute(i);
    }

    public void fuseNbt(ItemStack echo, ItemStack i) {
        NbtCompound nbt = echo.getOrCreateNbt();
        NbtCompound out = i.getOrCreateNbt();
        if (nbt.getBoolean(EchoShardRecipesMod.HAS_PARTICLE)) {
            int effect = nbt.getInt(EchoShardRecipesMod.PARTICLE);
            if (hasValidParticleItem(i)) {
                out.putBoolean(EchoShardRecipesMod.HAS_PARTICLE, true);
                out.putInt(EchoShardRecipesMod.PARTICLE, effect);
            }
        }
        if (nbt.getBoolean(EchoShardRecipesMod.HAS_ATTRIBUTE)) {
            String augment = nbt.getString(EchoShardRecipesMod.STORED_ATTRIBUTE);
            if (hasValidAttributedItem(i, augment)) {
                EchoShardRecipesMod.ATTRIBUTE_ITEMS.get(augment).processor.process(i);
                out.putBoolean(EchoShardRecipesMod.HAS_ATTRIBUTE, true);
                out.putString(EchoShardRecipesMod.ATTRIBUTE, augment);
            }
        }
        NbtCompound outputDisplay = out.getCompound(ItemStack.DISPLAY_KEY);
        NbtList outputLore = (NbtList) outputDisplay.get(ItemStack.LORE_KEY);
        if (outputLore == null) outputLore = new NbtList();
        NbtCompound nbtDisplay = nbt.getCompound(ItemStack.DISPLAY_KEY);
        NbtList nbtLore = (NbtList) nbtDisplay.get(ItemStack.LORE_KEY);
        if (nbtLore == null) nbtLore = new NbtList();
        outputLore.addAll(nbtLore);
        outputDisplay.put(ItemStack.LORE_KEY, outputLore);
        out.put(ItemStack.DISPLAY_KEY, outputDisplay);

        i.setNbt(out);
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        boolean validParticleTool = false;
        boolean validParticleMat = false;
        boolean validAttributeTool = false;
        String attribute = null;
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (itemStack.isOf(Items.ECHO_SHARD.asItem())) {
                if (hasParticleEffect(itemStack) && !validParticleMat) validParticleMat = true;
                else if (hasParticleEffect(itemStack) && validParticleMat) return false;
                if (hasAttribute(itemStack) && attribute == null) attribute = getShardAttribute(itemStack);
                else if (hasAttribute(itemStack) && attribute != null) return false;
            }
        }
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty() && !itemStack.isOf(Items.ECHO_SHARD)) {
                if (validParticleMat && hasValidParticleItem(itemStack) && !validParticleTool) validParticleTool = true;
                else if (validParticleMat && hasValidParticleItem(itemStack) && validParticleTool) return false;
                else if (validParticleMat && !hasValidParticleItem(itemStack)) return false;
                if (attribute != null && hasValidAttributedItem(itemStack, attribute) && !validAttributeTool) validAttributeTool = true;
                else if (attribute != null && hasValidAttributedItem(itemStack, attribute) && validAttributeTool) return false;
                else if (attribute != null && !hasValidAttributedItem(itemStack, attribute)) return false;
            }
        }
        return validParticleTool || validAttributeTool;
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        ItemStack aug = ItemStack.EMPTY;
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack temp = inventory.getStack(i);
            if (!temp.isEmpty() && !temp.isOf(Items.ECHO_SHARD)) aug = temp;
        }
        ItemStack item = aug.copy();
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack temp = inventory.getStack(i).copy();
            if (temp.isOf(Items.ECHO_SHARD) && temp.getNbt() != null) fuseNbt(temp, item);
        }
        return item;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EchoShardRecipesMod.ECHO_SHARD_AUGMENT;
    }

}
