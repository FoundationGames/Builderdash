package io.github.foundationgames.builderdash.game;

import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.map.BuilderdashMapConfig;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public interface BDGameConfig {
    BuilderdashMapConfig getMapConfig();

    PlayerConfig getLobbyConfig();

    String getGameName();

    void openActivity(GameSpace game, ServerWorld world, BuilderdashMap map);
}
