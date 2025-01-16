package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.mode.telephone.ui.GalleryReviewGui;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.PlayerRole;
import net.minecraft.server.world.ServerWorld;

public class TelephoneGalleryReviewRole extends PlayerRole.Flying {
    public final BDTelephoneActivity telephone;

    private GalleryReviewGui reviewGui = null;

    public TelephoneGalleryReviewRole(ServerWorld world, BDPlayer player, BDTelephoneActivity telephone) {
        super(world, player);
        this.telephone = telephone;
    }

    private GalleryReviewGui gui() {
        var playerE = this.player.player.getEntity(this.world);

        if (this.reviewGui == null && playerE != null) {
            this.reviewGui = new GalleryReviewGui(playerE, this.telephone);
        }

        return this.reviewGui;
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
