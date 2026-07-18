package io.github.foundationgames.builderdash.game;

import io.github.foundationgames.builderdash.game.element.title.StyledTitle;
import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.map.BuilderdashMapConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public interface BDGameConfig {
    BuilderdashMapConfig getMapConfig();

    WaitingLobbyConfig getLobbyConfig();

    String getGameName();

    StyledTitle makeTitle(Vec3 pos, float scale, Quaternionf rot);

    void openActivity(GameSpace game, ServerLevel world, BuilderdashMap map);
}
