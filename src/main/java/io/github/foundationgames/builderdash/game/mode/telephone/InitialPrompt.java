package io.github.foundationgames.builderdash.game.mode.telephone;

import xyz.nucleoid.plasmid.util.PlayerRef;

public class InitialPrompt {
    public final int seriesIndex;
    public final PlayerRef prompter;
    public String prompt;

    public InitialPrompt(int seriesIndex, PlayerRef prompter) {
        this.seriesIndex = seriesIndex;
        this.prompter = prompter;
    }
}
