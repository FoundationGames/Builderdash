package io.github.foundationgames.builderdash.game.player;

import io.github.foundationgames.builderdash.tools.BDToolsState;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class PlayerRole {
    public final ServerWorld world;
    public final BDPlayer player;

    protected BDToolsState tools;

    public PlayerRole(ServerWorld world, BDPlayer player) {
        this.world = world;
        this.player = player;
    }

    public boolean handleChatMessage(SignedMessage signedMessage, MessageType.Parameters parameters) {
        return true;
    }

    public boolean canModifyAt(BlockPos pos) {
        return false;
    }

    public GameMode getGameMode() {
        return GameMode.ADVENTURE;
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
        public Flying(ServerWorld world, BDPlayer player) {
            super(world, player);
        }

        @Override
        public void init() {
            super.init();

            this.player.player.ifOnline(this.world, s -> {
                s.getInventory().clear();

                if (!s.getAbilities().allowFlying) {
                    s.getAbilities().allowFlying = true;
                    s.sendAbilitiesUpdate();
                }
            });
        }
    }
}
