package io.github.foundationgames.builderdash.tools.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.foundationgames.builderdash.tools.BDToolsItems;
import io.github.foundationgames.builderdash.tools.item.FilterItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BDToolboxGui extends SimpleGui {
    public static final Component TITLE = Component.translatable("gui.builderdash.toolbox");

    public BDToolboxGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x3, player, false);

        setTitle(TITLE);

        this.setSlot(1, item(BDToolsItems.FILL));
        this.setSlot(4, item(BDToolsItems.SPHERE));
        this.setSlot(7, item(BDToolsItems.CYLINDER));

        this.setSlot(11, item(BDToolsItems.UNDO));
        this.setSlot(12, item(BDToolsItems.REDO));

        this.setSlot(14, item(FilterItemStack.createWhitelist()));
        this.setSlot(15, item(FilterItemStack.createBlacklist()));

        this.setSlot(19, item(BDToolsItems.BRUSH_SMALL));
        this.setSlot(22, item(BDToolsItems.BRUSH_MED));
        this.setSlot(25, item(BDToolsItems.BRUSH_LARGE));
    }

    private GuiElementBuilder item(ItemStack item) {
        return GuiElementBuilder.from(item)
                .setCallback(clickType -> player.getInventory().add(item.copy()));
    }

    private GuiElementBuilder item(Item item) {
        return item(item.getDefaultInstance());
    }
}
