package io.github.foundationgames.builderdash.game.mode.telephone.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.HotbarGui;
import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GalleryReviewGui extends HotbarGui {
    public static final Text VIEW_NEXT = Text.translatable("item.builderdash.telephone.view_next").formatted(Formatting.YELLOW);
    public static final Text VIEW_PREVIOUS = Text.translatable("item.builderdash.telephone.view_previous").formatted(Formatting.YELLOW);
    public static final Text END_GALLERY = Text.translatable("item.builderdash.telephone.end_gallery").formatted(Formatting.RED);

    public final BDTelephoneActivity telephone;

    public GalleryReviewGui(ServerPlayerEntity player, BDTelephoneActivity telephone) {
        super(player);
        this.telephone = telephone;

        this.setSlot(0, new GuiElementBuilder().setItem(Items.TIPPED_ARROW)
                .setComponent(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.HEALING))
                .setName(VIEW_PREVIOUS).glow().setCallback(clickType -> this.telephone.galleryReviewPrevious()));
        this.setSlot(1, new GuiElementBuilder().setItem(Items.TIPPED_ARROW)
                .setComponent(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.LUCK))
                .setName(VIEW_NEXT).glow().setCallback(clickType -> this.telephone.galleryReviewNext()));
        this.setSlot(2, new GuiElementBuilder().setItem(Items.FIELD_MASONED_BANNER_PATTERN)
                .setName(END_GALLERY).glow().setCallback(clickType -> this.telephone.galleryContinue()));
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }
}
