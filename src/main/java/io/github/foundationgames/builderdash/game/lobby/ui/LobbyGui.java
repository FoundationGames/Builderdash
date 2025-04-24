package io.github.foundationgames.builderdash.game.lobby.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.HotbarGui;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.game.lobby.LobbyPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.impl.game.common.ui.element.LeaveGameWaitingLobbyUiElement;

public class LobbyGui extends HotbarGui {
    public static final Text YOU_ARE_READY = Text.translatable("item.builderdash.lobby.you_are_ready").formatted(Formatting.AQUA);
    public static final Text YOU_ARE_NOT_READY = Text.translatable("item.builderdash.lobby.you_are_not_ready").formatted(Formatting.RED);

    private final LobbyPlayer player;

    public LobbyGui(ServerPlayerEntity player, LobbyPlayer lobbyPlayer) {
        super(player);

        this.player = lobbyPlayer;
        this.updateReadyItem();

        this.setSlot(8, new LeaveGameWaitingLobbyUiElement(lobbyPlayer.lobby.gameSpace, player).createMainElement());
    }

    public void updateReadyItem() {
        var el = new GuiElementBuilder().setItem(Items.PLAYER_HEAD);
        if (player.ready) {
            this.setSlot(0, el
                    .setComponent(DataComponentTypes.PROFILE, BDUtil.skinProfile(BDUtil.HEAD_READY[1]))
                    .setName(YOU_ARE_READY).glow().setCallback(clickType -> {
                        this.player.updateReady(false);
                        this.updateReadyItem();
                    }));
        } else {
            this.setSlot(0, el
                    .setComponent(DataComponentTypes.PROFILE, BDUtil.skinProfile(BDUtil.HEAD_READY[0]))
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
    public boolean onClickEntity(int entityId, EntityInteraction type, boolean isSneaking, Vec3d interactionPos) {
        super.onClickEntity(entityId, type, isSneaking, interactionPos);
        return true;
    }

    @Override
    public boolean canPlayerClose() {
        return false;
    }
}
