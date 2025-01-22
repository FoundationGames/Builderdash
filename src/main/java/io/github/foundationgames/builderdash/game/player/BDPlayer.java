package io.github.foundationgames.builderdash.game.player;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class BDPlayer {
    public final ServerWorld world;
    public final PlayerRef player;
    public PlayerRole currentRole;

    public int score;

    public BDPlayer(ServerWorld world, PlayerRef player) {
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
        this.player.ifOnline(this.world, s -> s.changeGameMode(role.getGameMode()));
        role.init();
    }

    public Text displayName() {
        var p = this.player.getEntity(this.world);
        if (p != null) {
            return p.getDisplayName();
        }
        return Text.empty();
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
