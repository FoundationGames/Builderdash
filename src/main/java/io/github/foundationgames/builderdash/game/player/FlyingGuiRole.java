package io.github.foundationgames.builderdash.game.player;

import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public abstract class FlyingGuiRole<G extends GuiInterface> extends PlayerRole.Flying {
    private G gui = null;

    public FlyingGuiRole(ServerLevel world, BDPlayer player) {
        super(world, player);
    }

    protected abstract G createGui(ServerPlayer entity);

    private G gui() {
        var playerE = this.player.player.getEntity(this.world);

        if (this.gui == null && playerE != null) {
            this.gui = createGui(playerE);
        }

        return this.gui;
    }

    @Override
    public void end() {
        super.end();

        var gui = this.gui();
        if (gui != null) {
            gui.close();
        }
    }

    @Override
    public void init() {
        super.init();

        var gui = this.gui();
        if (gui != null) {
            gui.open();
        }
    }
}
