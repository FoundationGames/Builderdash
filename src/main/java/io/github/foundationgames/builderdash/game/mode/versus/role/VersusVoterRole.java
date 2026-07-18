package io.github.foundationgames.builderdash.game.mode.versus.role;

import io.github.foundationgames.builderdash.game.mode.versus.BDVersusActivity;
import io.github.foundationgames.builderdash.game.mode.versus.ui.VoteBetweenPairGui;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.FlyingGuiRole;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class VersusVoterRole extends FlyingGuiRole<VoteBetweenPairGui> {
    public final BDVersusActivity versus;

    public VersusVoterRole(ServerLevel world, BDPlayer player, BDVersusActivity versus) {
        super(world, player);
        this.versus = versus;
    }

    @Override
    protected VoteBetweenPairGui createGui(ServerPlayer entity) {
        return new VoteBetweenPairGui(entity, this.versus);
    }
}
