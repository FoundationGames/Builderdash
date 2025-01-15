package io.github.foundationgames.builderdash.game.player;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class PlayerRole {
    public final ServerWorld world;
    public final BDPlayer player;

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

    public void init() {}

    public void tick() {}

    public void end() {}

    public static class Flying extends PlayerRole {
        public Flying(ServerWorld world, BDPlayer player) {
            super(world, player);
        }

        @Override
        public void init() {
            super.init();

            this.player.player.ifOnline(this.world, s -> {
                s.getInventory().clear();

                s.getAbilities().allowFlying = true;
                s.sendAbilitiesUpdate();
            });
        }
    }
}
