package io.github.foundationgames.builderdash.game.mode.versus.ui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.game.mode.versus.BDVersusActivity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class VoteBetweenPairGui extends HotbarGui {
    public static final String VOTE_FOR_BUILD = "item.builderdash.versus.vote_for_build";
    public final BDVersusActivity versus;

    private final List<VoteElement> voteElements = new ArrayList<>();

    public VoteBetweenPairGui(ServerPlayerEntity player, BDVersusActivity versus) {
        super(player);
        this.versus = versus;

        this.addVoteOption(3, 0, BDUtil.HEAD_MONITOR_1);
        this.addVoteOption(5, 1, BDUtil.HEAD_MONITOR_2);
    }

    private void addVoteOption(int slot, int buildIndex, String[] textures) {
        var el = new VoteElement(buildIndex, textures);
        this.setSlot(slot, el);
        this.voteElements.add(el);
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }

    public class VoteElement extends GuiElement {
        private final String[] textures;
        private final int buildIndex;

        public VoteElement(int buildIndex, String[] textures) {
            super(createIcon(textures, versus, player, buildIndex), (index, type, action) -> {});

            this.textures = textures;
            this.buildIndex = buildIndex;
        }

        private static ItemStack createIcon(String[] textures, BDVersusActivity versus, ServerPlayerEntity player, int buildIndex) {
            var stack = BDUtil.customHead(textures[versus.getVote(player) == buildIndex ? 1 : 0]);
            stack.set(DataComponentTypes.ITEM_NAME, Text.translatable(VOTE_FOR_BUILD, buildIndex + 1).formatted(Formatting.GREEN));
            return stack;
        }

        private void onClick(int index, ClickType type, SlotActionType action, SlotGuiInterface gui) {
            versus.setVote(player, buildIndex);
            voteElements.forEach(VoteElement::update);
        }

        public void update() {
            this.getItemStack().set(DataComponentTypes.PROFILE,
                    BDUtil.skinProfile(textures[versus.getVote(player) == buildIndex ? 1 : 0]));
        }

        @Override
        public ClickCallback getGuiCallback() {
            return this::onClick;
        }
    }
}
