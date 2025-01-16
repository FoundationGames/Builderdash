package io.github.foundationgames.builderdash.game.mode.telephone.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.HotbarGui;
import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GalleryControlGui extends HotbarGui {
    public static final Text CONTINUE = Text.translatable("item.builderdash.telephone.continue").formatted(Formatting.GOLD);

    public final BDTelephoneActivity telephone;

    public GalleryControlGui(ServerPlayerEntity player, BDTelephoneActivity telephone) {
        super(player);
        this.telephone = telephone;

        this.setSlot(0, new GuiElementBuilder().setItem(Items.GLOBE_BANNER_PATTERN)
                .setItemName(CONTINUE).glow().setCallback(clickType -> this.telephone.galleryContinue()));
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }
}
