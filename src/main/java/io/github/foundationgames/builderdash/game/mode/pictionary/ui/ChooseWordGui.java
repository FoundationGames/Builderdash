package io.github.foundationgames.builderdash.game.mode.pictionary.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ChooseWordGui extends SimpleGui {
    public static final Text TITLE = Text.translatable("gui.builderdash.pictionary.choose_word");
    public static final int[] WORD_IDX_TO_SLOT_ID = {1, 4, 7};

    public final String[] word1;
    public final String[] word2;
    public final String[] word3;

    public boolean stayOpen = true;

    public ChooseWordGui(ServerPlayerEntity player, String[] word1, String[] word2, String[] word3) {
        super(ScreenHandlerType.GENERIC_9X1, player, false);
        this.word1 = word1;
        this.word2 = word2;
        this.word3 = word3;

        setTitle(TITLE);

        addWordItem(0, word1);
        addWordItem(1, word2);
        addWordItem(2, word3);

        System.out.printf("Opened for player %s%n", player);
    }

    private void addWordItem(int index, String[] word) {
        this.setSlot(WORD_IDX_TO_SLOT_ID[index], new GuiElementBuilder(Items.BOOK)
                .setItemName(Text.literal(word[0]))
                .setCallback(clickType -> this.wordChosen(word)));
    }

    @Override
    public boolean canPlayerClose() {
        return !this.stayOpen;
    }

    public void wordChosen(String[] word) {
    }
}
