package io.github.foundationgames.builderdash.game.sound;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public record Chord(IntList formula) {
    public static final Chord M3 = of(1, 5);
    public static final Chord P5 = of(1, 8);
    public static final Chord P8 = of(1, 12);
    public static final Chord MAJ = of(1, 5, 8);
    public static final Chord MIN = of(1, 4, 8);
    public static final Chord MAJ7 = of(1, 5, 8, 13);
    public static final Chord MIN7 = of(1, 4, 8, 11);
    public static final Chord V7 = of(1, 5, 8, 11);
    public static final Chord DIM7 = of(1, 4, 7, 10);
    public static final Chord SUS2 = of(1, 3, 8);
    public static final Chord SUS4 = of(1, 6, 8);

    public static Chord of(int... formula) {
        return new Chord(new IntArrayList(formula));
    }

    public Chord in(int root) {
        if (formula().isEmpty()) {
            return this;
        }

        var nf = new IntArrayList();
        int os = root - formula().getInt(0);

        for (int i : formula()) {
            nf.add(i + os);
        }

        return new Chord(nf);
    }

    public Chord without(int... indices) {
        var nf = new IntArrayList(this.formula());
        for (int i : indices) nf.removeInt(i);

        return new Chord(nf);
    }

    public Chord reverse() {
        return new Chord(new IntArrayList(this.formula().reversed()));
    }
}
