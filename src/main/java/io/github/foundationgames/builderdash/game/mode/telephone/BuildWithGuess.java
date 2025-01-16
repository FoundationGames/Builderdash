package io.github.foundationgames.builderdash.game.mode.telephone;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class BuildWithGuess {
    public final PlayerRef builder;
    public final @Nullable PlayerRef guesser;
    public BuildZone build;
    public @Nullable String guess = null;

    public BuildWithGuess(PlayerRef builder, @Nullable PlayerRef guesser) {
        this.builder = builder;
        this.guesser = guesser;
    }
}
