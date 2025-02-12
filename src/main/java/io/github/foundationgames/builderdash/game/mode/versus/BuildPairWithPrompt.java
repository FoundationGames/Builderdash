package io.github.foundationgames.builderdash.game.mode.versus;

import io.github.foundationgames.builderdash.game.map.BuildZone;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class BuildPairWithPrompt {
    public final String prompt;
    public final PlayerRef[] builders = {null, null};
    public final BuildZone[] builds = {null, null};

    public BuildPairWithPrompt(String prompt) {
        this.prompt = prompt;
    }
}
