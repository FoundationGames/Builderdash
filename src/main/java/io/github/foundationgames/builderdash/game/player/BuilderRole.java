package io.github.foundationgames.builderdash.game.player;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class BuilderRole extends PlayerRole {
    public final BuildZone buildZone;
    private BDToolsState tools;

    public BuilderRole(ServerWorld world, BDPlayer player, BuildZone buildZone) {
        super(world, player);
        this.buildZone = buildZone;
    }

    @Override
    public boolean canModifyAt(BlockPos pos) {
        return buildZone.buildSafeArea().contains(pos);
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.CREATIVE;
    }

    @Override
    protected BDToolsState createTools() {
        return new BDToolsState(world.getServer(), player.player, 24, buildZone.buildSafeArea());
    }
}
