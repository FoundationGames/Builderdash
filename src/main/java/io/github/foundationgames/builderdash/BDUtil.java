package io.github.foundationgames.builderdash;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.TemplateRegion;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

import java.util.stream.IntStream;

public enum BDUtil {;
    public static final String SEC_REMAINING = "message.builderdash.seconds_remaining";

    public static TemplateRegion regionOrThrow(Identifier mapId, MapTemplate template, String marker) throws GameOpenException {
        var region = template.getMetadata().getFirstRegion(marker);
        if (region == null) {
            throw new GameOpenException(Text.literal(String.format("Map %s is missing region '%s'", mapId, marker)));
        }

        return region;
    }

    public static IntList range(int startInc, int endExc) {
        return new IntArrayList(IntStream.range(startInc, endExc).toArray());
    }

    // Mutates <ints>
    public static IntList circshift(IntList ints, int by) {
        int[] copy = new int[ints.size()];
        ints.toArray(copy);

        for (int i = 0; i < copy.length; i++) {
            ints.set(i, copy[(i + by) % copy.length]);
        }

        return ints;
    }
}
