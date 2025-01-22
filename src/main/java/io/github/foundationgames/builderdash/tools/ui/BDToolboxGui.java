package io.github.foundationgames.builderdash.tools.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import io.github.foundationgames.builderdash.tools.BDToolsItems;
import io.github.foundationgames.builderdash.tools.item.FilterItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BDToolboxGui extends SimpleGui {
    public static final Text TITLE = Text.translatable("gui.builderdash.toolbox");

    public BDToolboxGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

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
                .setCallback(clickType -> player.getInventory().insertStack(item.copy()));
    }

    private GuiElementBuilder item(Item item) {
        return item(item.getDefaultStack());
    }
}
