package io.github.foundationgames.builderdash.game.mode.pictionary;

import com.mojang.serialization.Codec;

import java.util.Arrays;
import java.util.List;

public record WordList(List<String[]> words) {
    public static final Codec<String[]> WORD_CODEC = Codec.withAlternative(
            Codec.STRING.xmap(s -> s.split("="), a -> String.join("=", a)),
            Codec.list(Codec.STRING).xmap(
                    l -> {
                        var arr = new String[l.size()];
                        l.toArray(arr);
                        return arr;
                    },
                    Arrays::asList
            )
    );

    public static final Codec<WordList> CODEC = Codec.list(WORD_CODEC).xmap(WordList::new, WordList::words);
}
