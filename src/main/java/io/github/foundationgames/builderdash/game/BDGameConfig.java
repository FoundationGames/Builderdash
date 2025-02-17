package io.github.foundationgames.builderdash.game;

import io.github.foundationgames.builderdash.game.element.title.StyledTitle;
import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.map.BuilderdashMapConfig;
import net.minecraft.server.world.ServerWorld;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public interface BDGameConfig {
    BuilderdashMapConfig getMapConfig();

    PlayerConfig getLobbyConfig();

    String getGameName();

    StyledTitle makeTitle(Vec3d pos, float scale, Quaternionf rot);

    void openActivity(GameSpace game, ServerWorld world, BuilderdashMap map);
}
