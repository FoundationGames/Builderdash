package io.github.foundationgames.builderdash.game.player;

import io.github.foundationgames.builderdash.tools.BDToolsState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;

public class PlayerRole {
    public final ServerLevel world;
    public final BDPlayer player;

    protected BDToolsState tools;

    public PlayerRole(ServerLevel world, BDPlayer player) {
        this.world = world;
        this.player = player;
    }

    public boolean handleChatMessage(PlayerChatMessage signedMessage, ChatType.Bound parameters) {
        return true;
    }

    public boolean canModifyAt(BlockPos pos) {
        return false;
    }

    public GameType getGameMode() {
        return GameType.ADVENTURE;
    }

    public void init() {
        this.tools = createTools();
    }

    public void tick() {}

    public void end() {
        if (this.tools != null) {
            this.tools.destroy();
            this.tools = null;
        }
    }

    protected BDToolsState createTools() {
        return new BDToolsState.Forbidden(world.getServer(), player.player, 1, null);
    }

    public static class Flying extends PlayerRole {
        public Flying(ServerLevel world, BDPlayer player) {
            super(world, player);
        }

        @Override
        public void init() {
            super.init();

            this.player.player.ifOnline(this.world, s -> {
                s.getInventory().clearContent();

                if (!s.getAbilities().mayfly) {
                    s.getAbilities().mayfly = true;
                    s.onUpdateAbilities();
                }
            });
        }
    }
}
