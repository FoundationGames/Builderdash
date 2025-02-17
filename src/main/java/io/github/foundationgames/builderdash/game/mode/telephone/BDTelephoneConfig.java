package io.github.foundationgames.builderdash.game.mode.telephone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.game.BDGameConfig;
import io.github.foundationgames.builderdash.game.element.title.StyledTitle;
import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.map.BuilderdashMapConfig;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

public record BDTelephoneConfig(
        WaitingLobbyConfig players, int buildTime, int guessTime, boolean doubleRounds, BuilderdashMapConfig map
) implements BDGameConfig {
    public static final String TELEPHONE = "telephone";

    public static final Identifier DEFAULT_CONFIG = Builderdash.id("telephone");
    public static final Identifier FAST_CONFIG = Builderdash.id("telephone_fast");
    public static final Identifier DOUBLE_CONFIG = Builderdash.id("telephone_double_rounds");
    public static final Identifier DOUBLE_FAST_CONFIG = Builderdash.id("telephone_fast_double_rounds");
    public static final Identifier TEST_CONFIG = Builderdash.id("telephone_test");

    public static final MapCodec<BDTelephoneConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(BDTelephoneConfig::players),
            Codec.INT.fieldOf("build_time").forGetter(BDTelephoneConfig::buildTime),
            Codec.INT.fieldOf("guess_time").forGetter(BDTelephoneConfig::guessTime),
            Codec.BOOL.fieldOf("double_rounds").forGetter(BDTelephoneConfig::doubleRounds),
            BuilderdashMapConfig.CODEC.fieldOf("map").forGetter(BDTelephoneConfig::map)
    ).apply(instance, BDTelephoneConfig::new));

    @Override
    public BuilderdashMapConfig getMapConfig() {
        return map();
    }

    @Override
    public WaitingLobbyConfig getLobbyConfig() {
        return players();
    }

    @Override
    public String getGameName() {
        return TELEPHONE;
    }

    @Override
    public StyledTitle makeTitle(Vec3d pos, float scale, Quaternionf rot) {
        return StyledTitle.forMinigame(pos, scale, rot, TELEPHONE, 0x8a24ff);
    }

    @Override
    public void openActivity(GameSpace game, ServerWorld world, BuilderdashMap map) {
        BDTelephoneActivity.open(game, world, map, this);
    }
}
