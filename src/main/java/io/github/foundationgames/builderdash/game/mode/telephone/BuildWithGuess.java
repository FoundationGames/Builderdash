package io.github.foundationgames.builderdash.game.mode.telephone;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

public class BuildWithGuess {
    public final int seriesIndex;
    public final PlayerRef builder;
    public final PlayerRef guesser;
    public BuildZone build;
    public String guess = null;

    public BuildWithGuess(int seriesIndex, PlayerRef builder, PlayerRef guesser) {
        this.seriesIndex = seriesIndex;
        this.builder = builder;
        this.guesser = guesser;
    }
}
