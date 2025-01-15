package io.github.foundationgames.builderdash.game.player;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class BuilderRole extends PlayerRole {
    public final BuildZone buildZone;

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
}
