package io.github.foundationgames.builderdash.game.mode.versus.ui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SlotBasedGui;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.game.mode.versus.BDVersusActivity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public class VoteBetweenPairGui extends HotbarGui {
    public static final String VOTE_FOR_BUILD = "item.builderdash.versus.vote_for_build";
    public final BDVersusActivity versus;

    private final List<VoteElement> voteElements = new ArrayList<>();

    public VoteBetweenPairGui(ServerPlayer player, BDVersusActivity versus) {
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

    public class VoteElement implements GuiElement {
        private final ItemStack displayItem;
        private final String[] textures;
        private final int buildIndex;

        public VoteElement(int buildIndex, String[] textures) {
            this.displayItem = createIcon(textures, versus, player, buildIndex);
            this.textures = textures;
            this.buildIndex = buildIndex;
        }

        private static ItemStack createIcon(String[] textures, BDVersusActivity versus, ServerPlayer player, int buildIndex) {
            var stack = BDUtil.customHead(textures[versus.getVote(player) == buildIndex ? 1 : 0]);
            stack.set(DataComponents.ITEM_NAME, Component.translatable(VOTE_FOR_BUILD, buildIndex + 1).withStyle(ChatFormatting.GREEN));
            return stack;
        }

        private void onClick(int index, ClickType type, ContainerInput action, SlotBasedGui gui) {
            versus.setVote(player, buildIndex);
            voteElements.forEach(VoteElement::update);
        }

        public void update() {
            this.getItemStack().set(DataComponents.PROFILE,
                    BDUtil.skinProfile(textures[versus.getVote(player) == buildIndex ? 1 : 0]));
        }

        @Override
        public ItemStack getItemStack() {
            return displayItem;
        }

        @Override
        public ClickCallback getGuiCallback() {
            return this::onClick;
        }
    }
}
