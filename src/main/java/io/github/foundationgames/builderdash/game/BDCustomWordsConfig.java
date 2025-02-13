package io.github.foundationgames.builderdash.game;

public interface BDCustomWordsConfig<C extends BDCustomWordsConfig<C>> extends BDGameConfig {
    C withCustomWords(CustomWordsPersistentState state);
}
