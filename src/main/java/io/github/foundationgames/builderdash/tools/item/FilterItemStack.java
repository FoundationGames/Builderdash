package io.github.foundationgames.builderdash.tools.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;

public enum FilterItemStack {;
    public static final Component FILTER_ACTIVE = Component.translatable("tooltip.builderdash.tool.filter_active").withStyle(ChatFormatting.WHITE);
    public static final Component FILTER_WL = Component.translatable("tooltip.builderdash.tool.filter_whitelist").withStyle(ChatFormatting.GRAY);
    public static final Component FILTER_BL = Component.translatable("tooltip.builderdash.tool.filter_blacklist").withStyle(ChatFormatting.GRAY);
    public static final Component FILTER_USE_AIR = Component.translatable("tooltip.builderdash.tool.filter_use_air").withStyle(ChatFormatting.GRAY);

    public static ItemStack createWhitelist() {
        var stack = Items.LIGHT_BLUE_BUNDLE.getDefaultInstance();
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.builderdash.tool_filter_whitelist").withStyle(ChatFormatting.GOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                FILTER_ACTIVE, FILTER_WL, FILTER_USE_AIR
        )));

        var data = new CompoundTag();
        data.putBoolean("builderdash:filter", false);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));

        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }

    public static ItemStack createBlacklist() {
        var stack = Items.ORANGE_BUNDLE.getDefaultInstance();
        stack.set(DataComponents.ITEM_NAME, Component.translatable("item.builderdash.tool_filter_blacklist").withStyle(ChatFormatting.GOLD));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                FILTER_ACTIVE, FILTER_BL, FILTER_USE_AIR
        )));

        var data = new CompoundTag();
        data.putBoolean("builderdash:filter", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));

        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        return stack;
    }
}
