package io.github.foundationgames.builderdash.game.mode.versus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.game.BDCustomWordsConfig;
import io.github.foundationgames.builderdash.game.BDGameConfig;
import io.github.foundationgames.builderdash.game.CustomWordsPersistentState;
import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.map.BuilderdashMapConfig;
import io.github.foundationgames.builderdash.game.mode.pictionary.WordList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.api.game.GameSpace;
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig;

import java.util.ArrayList;

public record BDVersusConfig(
        WaitingLobbyConfig players, WordList wordList, int buildTime, int voteTime, float pointRoundMul,
        boolean doubleRounds, BuilderdashMapConfig map
) implements BDGameConfig, BDCustomWordsConfig<BDVersusConfig> {
    public static final Identifier DEFAULT_CONFIG = Builderdash.id("versus");
    public static final Identifier FAST_CONFIG = Builderdash.id("versus_fast");
    public static final Identifier DOUBLE_CONFIG = Builderdash.id("versus_double_rounds");
    public static final Identifier DOUBLE_FAST_CONFIG = Builderdash.id("versus_fast_double_rounds");
    public static final Identifier TEST_CONFIG = Builderdash.id("versus_test");

    public static final MapCodec<BDVersusConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WaitingLobbyConfig.CODEC.fieldOf("players").forGetter(BDVersusConfig::players),
            WordList.CODEC.fieldOf("prompt_list").forGetter(BDVersusConfig::wordList),
            Codec.INT.fieldOf("build_time").forGetter(BDVersusConfig::buildTime),
            Codec.INT.fieldOf("vote_time").forGetter(BDVersusConfig::voteTime),
            Codec.FLOAT.fieldOf("point_multiplier_per_round").forGetter(BDVersusConfig::pointRoundMul),
            Codec.BOOL.fieldOf("double_rounds").forGetter(BDVersusConfig::doubleRounds),
            BuilderdashMapConfig.CODEC.fieldOf("map").forGetter(BDVersusConfig::map)
    ).apply(instance, BDVersusConfig::new));

    @Override
    public BuilderdashMapConfig getMapConfig() {
        return map();
    }

    @Override
    public WaitingLobbyConfig getLobbyConfig() {
        return players();
    }

    @Override
    public BDVersusConfig withCustomWords(CustomWordsPersistentState state) {
        var words = new ArrayList<>(this.wordList().words());

        if (state.customWords.size() > 0) {
            if (state.replaceDefault) {
                words.clear();
            }

            words.addAll(state.customWords);
        }

        return new BDVersusConfig(
                players(), new WordList(words), buildTime(), voteTime(), pointRoundMul(), doubleRounds(), map()
        );
    }

    @Override
    public void openActivity(GameSpace game, ServerWorld world, BuilderdashMap map) {
        BDVersusActivity.open(game, world, map, this);
    }
}
