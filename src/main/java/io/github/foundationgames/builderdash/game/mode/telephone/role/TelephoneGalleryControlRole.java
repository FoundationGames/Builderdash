package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.mode.telephone.ui.GalleryControlGui;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.FlyingGuiRole;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TelephoneGalleryControlRole extends FlyingGuiRole<GalleryControlGui> {
    public final BDTelephoneActivity telephone;

    public TelephoneGalleryControlRole(ServerLevel world, BDPlayer player, BDTelephoneActivity telephone) {
        super(world, player);
        this.telephone = telephone;
    }

    @Override
    protected GalleryControlGui createGui(ServerPlayer entity) {
        return new GalleryControlGui(entity, this.telephone);
    }
}
