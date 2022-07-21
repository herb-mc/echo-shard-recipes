package com.herb_mc.echo_shard_recipes.recipes;

import com.herb_mc.echo_shard_recipes.EchoShardRecipesMod;
import com.herb_mc.echo_shard_recipes.api.ServersideRecipe;
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
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Map;

import static com.herb_mc.echo_shard_recipes.EchoShardRecipesMod.WANDS;
import static com.herb_mc.echo_shard_recipes.helper.Misc.getText;

public class EchoWandRecipe extends SpecialCraftingRecipe implements ServersideRecipe {

    private boolean hasSpellFocus(ItemStack in) {
        return in.getNbt() != null && (in.getNbt().getBoolean("spell_focus"));
    }

    private String containsSpell(ItemStack i) {
        EchoShardRecipesMod.LOGGER.info("{}", i);
        for (Map.Entry<String, EchoShardRecipesMod.WandItem> w : WANDS.entrySet())
            if (i.isOf(w.getValue().item.asItem())) return w.getKey();
        return null;
    }

    public EchoWandRecipe(Identifier id) {
        super(id);
    }

    private String spell;

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        boolean hasWand = false;
        boolean hasSpellFocus = false;
        spell = null;
        for(int i = 0; i < inventory.size(); ++i) {
            ItemStack itemStack = inventory.getStack(i);
            if (!itemStack.isEmpty()) {
                if (itemStack.isOf(Items.STICK.asItem())) hasWand = true;
                else if (hasSpellFocus(itemStack)) hasSpellFocus = true;
                else if (containsSpell(itemStack) != null) spell = containsSpell(itemStack);
                else return false;
            }
        }
        return hasWand && hasSpellFocus && spell != null;
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        ItemStack out = new ItemStack(Items.STICK);
        addSpell(out, spell);
        return out;
    }

    public void addSpell(ItemStack i, String spell) {
        EchoShardRecipesMod.WandItem w = WANDS.get(spell);
        NbtCompound out = i.getOrCreateNbt();
        out.putBoolean(EchoShardRecipesMod.HAS_ATTRIBUTE, true);
        i.setCustomName(MutableText.of(Text.of(w.spell + " Wand").getContent()).setStyle(Style.EMPTY.withItalic(false).withColor(w.color)));
        out.putString(EchoShardRecipesMod.ATTRIBUTE, "spell");
        out.putString(EchoShardRecipesMod.SPELL, spell);
        NbtCompound nbtDisplay = out.getCompound(ItemStack.DISPLAY_KEY);
        NbtList nbtLore = (NbtList) nbtDisplay.get(ItemStack.LORE_KEY);
        if (nbtLore == null) nbtLore = new NbtList();
        nbtLore.add(NbtString.of(Text.Serializer.toJson(getText("Spell", w.spell, w.color))));
        nbtDisplay.put(ItemStack.LORE_KEY, nbtLore);
        out.put(ItemStack.DISPLAY_KEY, nbtDisplay);
        i.setNbt(out);
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EchoShardRecipesMod.ECHO_WAND;
    }

}
