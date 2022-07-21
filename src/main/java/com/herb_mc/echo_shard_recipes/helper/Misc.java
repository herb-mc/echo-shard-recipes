package com.herb_mc.echo_shard_recipes.helper;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Misc {

    public static MutableText getText(String type, String value, Formatting f) {
        return MutableText.of(Text.of(type + ": [").getContent()).setStyle(Style.EMPTY.withFormatting(Formatting.WHITE).withItalic(false).withBold(false))
                .append(MutableText.of(Text.of(value).getContent()).setStyle(Style.EMPTY.withFormatting(f).withItalic(false).withBold(false)))
                .append(MutableText.of(Text.of("]").getContent()).setStyle(Style.EMPTY.withFormatting(Formatting.WHITE).withItalic(false).withBold(false)));
    }

}
