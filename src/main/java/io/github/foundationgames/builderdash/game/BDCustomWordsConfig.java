package io.github.foundationgames.builderdash.game;

public interface BDCustomWordsConfig<C extends BDCustomWordsConfig<C>> {
    C withCustomWords(CustomWordsPersistentState state);
}
