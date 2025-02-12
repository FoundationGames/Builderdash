package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.mode.telephone.ui.GalleryControlGui;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.FlyingGuiRole;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class TelephoneGalleryControlRole extends FlyingGuiRole<GalleryControlGui> {
    public final BDTelephoneActivity telephone;

    public TelephoneGalleryControlRole(ServerWorld world, BDPlayer player, BDTelephoneActivity telephone) {
        super(world, player);
        this.telephone = telephone;
    }

    @Override
    protected GalleryControlGui createGui(ServerPlayerEntity entity) {
        return new GalleryControlGui(entity, this.telephone);
    }
}
