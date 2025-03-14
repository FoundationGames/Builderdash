package io.github.foundationgames.builderdash.game.lobby;

import io.github.foundationgames.builderdash.game.lobby.ui.LobbyGui;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class LobbyPlayer {
    public final BDLobbyActivity<?> lobby;
    public final PlayerRef player;
    public LobbyGui gui;
    public boolean ready = false;

    public LobbyPlayer(GameSpace space, BDLobbyActivity<?> lobby, PlayerRef player) {
        this.lobby = lobby;
        this.player = player;

        player.ifOnline(space, s -> {
            this.gui = new LobbyGui(s, this);
            this.gui.open();
        });
    }

    public void updateReady(boolean ready) {
        this.ready = ready;
        this.lobby.checkCanStart();
    }

    public void destroy() {
        this.gui.close();
    }
}
