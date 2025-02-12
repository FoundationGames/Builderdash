package io.github.foundationgames.builderdash.game.mode.telephone.role;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import io.github.foundationgames.builderdash.game.mode.telephone.BDTelephoneActivity;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.SubmissionBuilderRole;
import net.minecraft.server.world.ServerWorld;

public class TelephoneBuilderRole extends SubmissionBuilderRole {
    public final BDTelephoneActivity telephone;

    public final int seriesIndex;

    public TelephoneBuilderRole(ServerWorld world, BDPlayer player, BuildZone buildZone, BDTelephoneActivity telephone, int seriesIndex) {
        super(world, player, buildZone);
        this.telephone = telephone;
        this.seriesIndex = seriesIndex;
    }

    @Override
    protected void submit() {
        this.telephone.setBuilderFinishedStatus(this.player.player, true);
    }

    @Override
    protected void unsubmit() {
        this.telephone.setBuilderFinishedStatus(this.player.player, false);
    }
}
