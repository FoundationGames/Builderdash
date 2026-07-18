package io.github.foundationgames.builderdash.game.mode.telephone.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.HotbarGui;
import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

public class GalleryReviewGui extends HotbarGui {
    public static final Component VIEW_NEXT = Component.translatable("item.builderdash.telephone.view_next").withStyle(ChatFormatting.YELLOW);
    public static final Component VIEW_PREVIOUS = Component.translatable("item.builderdash.telephone.view_previous").withStyle(ChatFormatting.YELLOW);
    public static final Component END_GALLERY = Component.translatable("item.builderdash.telephone.end_gallery").withStyle(ChatFormatting.RED);

    public final BDTelephoneActivity telephone;

    public GalleryReviewGui(ServerPlayer player, BDTelephoneActivity telephone) {
        super(player);
        this.telephone = telephone;

        this.setSlot(0, new GuiElementBuilder().setItem(Items.TIPPED_ARROW)
                .setComponent(DataComponents.POTION_CONTENTS, new PotionContents(Potions.HEALING))
                .setName(VIEW_PREVIOUS).glow().setCallback(clickType -> this.telephone.galleryReviewPrevious()));
        this.setSlot(1, new GuiElementBuilder().setItem(Items.TIPPED_ARROW)
                .setComponent(DataComponents.POTION_CONTENTS, new PotionContents(Potions.LUCK))
                .setName(VIEW_NEXT).glow().setCallback(clickType -> this.telephone.galleryReviewNext()));
        this.setSlot(2, new GuiElementBuilder().setItem(Items.FIELD_MASONED_BANNER_PATTERN)
                .setName(END_GALLERY).glow().setCallback(clickType -> this.telephone.galleryContinue()));
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }
}
