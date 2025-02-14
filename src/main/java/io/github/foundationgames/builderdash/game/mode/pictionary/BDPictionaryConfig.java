package io.github.foundationgames.builderdash.game.mode.pictionary;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.game.BDCustomWordsConfig;
import io.github.foundationgames.builderdash.game.CustomWordsPersistentState;
import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.map.BuilderdashMapConfig;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

import java.util.ArrayList;

public record BDPictionaryConfig(
        PlayerConfig players, WordList wordList, int wordChooseTime, int maxBuildTime, int minBuildTime,
        float guesserThreshold, float revealPercent, int guessCloseness, boolean doubleRounds, BuilderdashMapConfig map
) implements BDCustomWordsConfig<BDPictionaryConfig> {
    public static final String PICTIONARY = "pictionary";

    public static final Identifier DEFAULT_CONFIG = Builderdash.id("pictionary");
    public static final Identifier DOUBLE_CONFIG = Builderdash.id("pictionary_double_rounds");
    public static final Identifier TEST_CONFIG = Builderdash.id("pictionary_test");

    public static final MapCodec<BDPictionaryConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(BDPictionaryConfig::players),
            WordList.CODEC.fieldOf("word_list").forGetter(BDPictionaryConfig::wordList),
            Codec.INT.fieldOf("word_choose_time").forGetter(BDPictionaryConfig::wordChooseTime),
            Codec.INT.fieldOf("max_build_time").forGetter(BDPictionaryConfig::maxBuildTime),
            Codec.INT.fieldOf("min_build_time").forGetter(BDPictionaryConfig::minBuildTime),
            Codec.FLOAT.optionalFieldOf("guesser_threshold", 0.6f).forGetter(BDPictionaryConfig::guesserThreshold),
            Codec.FLOAT.optionalFieldOf("max_word_reveal_percent", 0.4f).forGetter(BDPictionaryConfig::guesserThreshold),
            Codec.INT.fieldOf("guess_closeness").forGetter(BDPictionaryConfig::guessCloseness),
            Codec.BOOL.fieldOf("double_rounds").forGetter(BDPictionaryConfig::doubleRounds),
            BuilderdashMapConfig.CODEC.fieldOf("map").forGetter(BDPictionaryConfig::map)
    ).apply(instance, BDPictionaryConfig::new));

    @Override
    public BDPictionaryConfig withCustomWords(CustomWordsPersistentState state) {
        var words = new ArrayList<>(this.wordList().words());

        if (state.customWords.size() > 0) {
            if (state.replaceDefault) {
                words.clear();
            }

            words.addAll(state.customWords);
        }

        return new BDPictionaryConfig(
                players(), new WordList(words), wordChooseTime(), maxBuildTime(), minBuildTime(), guesserThreshold(),
                revealPercent(), guessCloseness(), doubleRounds(), map()
        );
    }

    @Override
    public BuilderdashMapConfig getMapConfig() {
        return map();
    }

    @Override
    public PlayerConfig getLobbyConfig() {
        return players();
    }

    @Override
    public String getGameName() {
        return PICTIONARY;
    }

    @Override
    public void openActivity(GameSpace game, ServerWorld world, BuilderdashMap map) {
        BDPictionaryActivity.open(game, world, map, this);
    }
}
