package io.github.foundationgames.builderdash.game.lobby.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.HotbarGui;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.game.lobby.LobbyPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.plasmid.impl.game.common.ui.element.LeaveGameWaitingLobbyUiElement;

public class LobbyGui extends HotbarGui {
    public static final Component YOU_ARE_READY = Component.translatable("item.builderdash.lobby.you_are_ready").withStyle(ChatFormatting.AQUA);
    public static final Component YOU_ARE_NOT_READY = Component.translatable("item.builderdash.lobby.you_are_not_ready").withStyle(ChatFormatting.RED);

    private final LobbyPlayer player;

    public LobbyGui(ServerPlayer player, LobbyPlayer lobbyPlayer) {
        super(player);

        this.player = lobbyPlayer;
        this.updateReadyItem();

        this.setSlot(8, new LeaveGameWaitingLobbyUiElement(lobbyPlayer.lobby.gameSpace, player).createMainElement());
    }

    public void updateReadyItem() {
        var el = new GuiElementBuilder().setItem(Items.PLAYER_HEAD);
        if (player.ready) {
            this.setSlot(0, el
                    .setComponent(DataComponents.PROFILE, BDUtil.skinProfile(BDUtil.HEAD_READY[1]))
                    .setName(YOU_ARE_READY).glow().setCallback(clickType -> {
                        this.player.updateReady(false);
                        this.updateReadyItem();
                    }));
        } else {
            this.setSlot(0, el
                    .setComponent(DataComponents.PROFILE, BDUtil.skinProfile(BDUtil.HEAD_READY[0]))
                    .setName(YOU_ARE_NOT_READY).setCallback(clickType -> {
                        this.player.updateReady(true);
                        this.updateReadyItem();
                    }));
        }
    }

    @Override
    public boolean onHandSwing() {
        super.onHandSwing();
        return true;
    }

    @Override
    public boolean onClickBlock(BlockHitResult hitResult) {
        return true;
    }

    @Override
    public boolean onEntityInteracted(int entityId, InteractionHand hand, boolean isSneaking, Vec3 interactionPos) {
        super.onEntityInteracted(entityId, hand, isSneaking, interactionPos);
        return true;
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }
}
