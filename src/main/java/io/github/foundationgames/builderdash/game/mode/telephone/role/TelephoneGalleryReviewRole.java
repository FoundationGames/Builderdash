package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.mode.telephone.ui.GalleryReviewGui;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.FlyingGuiRole;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class TelephoneGalleryReviewRole extends FlyingGuiRole<GalleryReviewGui> {
    public final BDTelephoneActivity telephone;

    public TelephoneGalleryReviewRole(ServerWorld world, BDPlayer player, BDTelephoneActivity telephone) {
        super(world, player);
        this.telephone = telephone;
    }

    @Override
    protected GalleryReviewGui createGui(ServerPlayerEntity entity) {
        return new GalleryReviewGui(entity, this.telephone);
    }
}
