package io.github.foundationgames.builderdash.tools.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public enum FilterItemStack {;
    public static final Text FILTER_ACTIVE = Text.translatable("tooltip.builderdash.tool.filter_active").formatted(Formatting.WHITE);
    public static final Text FILTER_WL = Text.translatable("tooltip.builderdash.tool.filter_whitelist").formatted(Formatting.GRAY);
    public static final Text FILTER_BL = Text.translatable("tooltip.builderdash.tool.filter_blacklist").formatted(Formatting.GRAY);
    public static final Text FILTER_USE_AIR = Text.translatable("tooltip.builderdash.tool.filter_use_air").formatted(Formatting.GRAY);

    public static ItemStack createWhitelist() {
        var stack = Items.BUNDLE.getDefaultStack();
        stack.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.builderdash.tool_filter_whitelist").formatted(Formatting.GOLD));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FILTER_ACTIVE, FILTER_WL, FILTER_USE_AIR
        )));

        var data = new NbtCompound();
        data.putBoolean("builderdash:filter", false);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));

        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }

    public static ItemStack createBlacklist() {
        var stack = Items.BUNDLE.getDefaultStack();
        stack.set(DataComponentTypes.ITEM_NAME, Text.translatable("item.builderdash.tool_filter_blacklist").formatted(Formatting.GOLD));
        stack.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                FILTER_ACTIVE, FILTER_BL, FILTER_USE_AIR
        )));

        var data = new NbtCompound();
        data.putBoolean("builderdash:filter", true);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(data));

        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }
}
