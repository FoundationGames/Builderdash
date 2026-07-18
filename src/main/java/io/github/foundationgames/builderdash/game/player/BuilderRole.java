package io.github.foundationgames.builderdash.game.player;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;

public class BuilderRole extends PlayerRole {
    public final BuildZone buildZone;
    private BDToolsState tools;

    public BuilderRole(ServerLevel world, BDPlayer player, BuildZone buildZone) {
        super(world, player);
        this.buildZone = buildZone;
    }

    @Override
    public boolean canModifyAt(BlockPos pos) {
        return buildZone.buildSafeArea().contains(pos);
    }

    @Override
    public GameType getGameMode() {
        return GameType.CREATIVE;
    }

    @Override
    protected BDToolsState createTools() {
        return new BDToolsState(world.getServer(), player.player, 24, buildZone.buildSafeArea());
    }
}
