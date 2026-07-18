package io.github.foundationgames.builderdash.game.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class BDPlayer {
    public final ServerLevel world;
    public final PlayerRef player;
    public PlayerRole currentRole;

    public int score;

    public BDPlayer(ServerLevel world, PlayerRef player) {
        this.world = world;
        this.player = player;

        this.currentRole = new PlayerRole(this.world, this);
    }

    public void notifyReconnect() {
        this.updateRole(this.currentRole);
    }

    public void updateRole(PlayerRole role) {
        if (this.currentRole != null) {
            this.currentRole.end();
        }

        this.currentRole = role;
        this.player.ifOnline(this.world, s -> {
            var m = role.getGameMode();
            if (s.gameMode.getGameModeForPlayer() != m) {
                s.setGameMode(m);
            }
        });
        role.init();
    }

    public Component displayName() {
        var p = this.player.getEntity(this.world);
        if (p != null) {
            return p.getDisplayName();
        }
        return Component.empty();
    }

    public void tick() {
        if (this.currentRole != null) {
            this.currentRole.tick();
        }
    }

    public void end() {
        if (this.currentRole != null) {
            this.currentRole.end();
        }
    }
}
