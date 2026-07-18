package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.mode.telephone.ui.GalleryReviewGui;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.FlyingGuiRole;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TelephoneGalleryReviewRole extends FlyingGuiRole<GalleryReviewGui> {
    public final BDTelephoneActivity telephone;

    public TelephoneGalleryReviewRole(ServerLevel world, BDPlayer player, BDTelephoneActivity telephone) {
        super(world, player);
        this.telephone = telephone;
    }

    @Override
    protected GalleryReviewGui createGui(ServerPlayer entity) {
        return new GalleryReviewGui(entity, this.telephone);
    }
}
