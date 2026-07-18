package io.github.foundationgames.builderdash.game.mode.telephone.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.HotbarGui;
import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;

public class GalleryControlGui extends HotbarGui {
    public static final Component CONTINUE = Component.translatable("item.builderdash.telephone.continue").withStyle(ChatFormatting.GOLD);

    public final BDTelephoneActivity telephone;

    public GalleryControlGui(ServerPlayer player, BDTelephoneActivity telephone) {
        super(player);
        this.telephone = telephone;

        this.setSlot(0, new GuiElementBuilder().setItem(Items.GLOBE_BANNER_PATTERN)
                .setName(CONTINUE).glow().setCallback(clickType -> this.telephone.galleryContinue()));
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }
}
