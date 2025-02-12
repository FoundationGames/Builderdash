package io.github.foundationgames.builderdash.game.mode.versus.role;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import io.github.foundationgames.builderdash.game.mode.versus.BDVersusActivity;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import io.github.foundationgames.builderdash.game.player.SubmissionBuilderRole;
import net.minecraft.server.world.ServerWorld;

public class VersusBuilderRole extends SubmissionBuilderRole {
    public final BDVersusActivity versus;

    public VersusBuilderRole(ServerWorld world, BDPlayer player, BuildZone buildZone, BDVersusActivity versus) {
        super(world, player, buildZone);
        this.versus = versus;
    }

    @Override
    protected void submit() {
        this.versus.setBuilderFinishedStatus(this.player.player, true);
    }

    @Override
    protected void unsubmit() {
        this.versus.setBuilderFinishedStatus(this.player.player, false);
    }
}
