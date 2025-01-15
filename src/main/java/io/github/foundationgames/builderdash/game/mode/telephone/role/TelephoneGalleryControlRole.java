package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.mode.telephone.ui.GalleryControlGui;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.PlayerRole;
import net.minecraft.server.world.ServerWorld;

public class TelephoneGalleryControlRole extends PlayerRole.Flying {
    public final BDTelephoneActivity telephone;

    private GalleryControlGui controlGui = null;

    public TelephoneGalleryControlRole(ServerWorld world, BDPlayer player, BDTelephoneActivity telephone) {
        super(world, player);
        this.telephone = telephone;
    }

    private GalleryControlGui gui() {
        var playerE = this.player.player.getEntity(this.world);

        if (this.controlGui == null && playerE != null) {
            this.controlGui = new GalleryControlGui(playerE, this.telephone);
        }

        return this.controlGui;
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
