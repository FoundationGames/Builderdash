package io.github.foundationgames.builderdash.game.sound;

import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.game.element.TickingAnimation;
import net.minecraft.block.NoteBlock;
import net.minecraft.network.packet.s2c.play.PlaySoundFromEntityS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import xyz.nucleoid.plasmid.util.PlayerRef;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public record SFX(Deque<Step> steps) {
    private static final Chord TRILL3 = Chord.of(1, 3, 1);

    public static final SFX FANFARE = SFX.builder()
            .note(SoundEvents.GOAT_HORN_SOUNDS.get(1), 1f, BDUtil.fSharp(6), 11)
            .note(SoundEvents.GOAT_HORN_SOUNDS.get(1), 0.8f, BDUtil.fSharp(6), -1)
            .sound(SoundEvents.GOAT_HORN_SOUNDS.get(5), 0.3f, 2f)
            .sound(SoundEvents.GOAT_HORN_SOUNDS.get(5), 0.3f, 1f)
            .sound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.4f, 1f).build();
    public static final SFX MINI_FANFARE = SFX.builder()
            .sound(SoundEvents.GOAT_HORN_SOUNDS.get(5), 0.3f, 1f)
            .sound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.4f, 1f).build();
    public static final SFX CLICK = SFX.builder()
            .sound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, 1f, 1f).build();
    public static final SFX NOTE_CLICK = SFX.builder()
            .sound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, 1f, 1f)
            .note(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1f, 6).build();
    public static final SFX HIGH_CLICK = SFX.builder()
            .sound(SoundEvents.BLOCK_NOTE_BLOCK_HAT, 1f, 1f)
            .note(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1f, 11)
            .note(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE, 1f, 11).build();

    public static final SFX BUILD_LAYER = SFX.builder()
            .sound(SoundEvents.BLOCK_COPPER_BULB_TURN_ON, 1, 1).build();
    public static final SFX BUILD_REVEAL = SFX.builder()
            .sound(SoundEvents.BLOCK_CONDUIT_DEACTIVATE, 0.5f, 1)
            .sound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.4f, 0.75f).build();

    public static final SFX PICTIONARY_CORRECT = SFX.poly(
            SFX.builder()
                    .note(SoundEvents.ENTITY_ALLAY_ITEM_THROWN, 1f, 1.02f, 8)
                    .arpeggio(SoundEvents.BLOCK_NOTE_BLOCK_BELL, 1f, TRILL3.in(8), 2, 1)
                    .build(),
            SFX.builder()
                    .sound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.3f, 1.5f)
                    .arpeggio(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1f, TRILL3.in(8), 2, 1)
                    .build()
    );
    public static final SFX PICTIONARY_CLOSE_GUESS = SFX.builder()
            .sound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 0.7f)
            .arpeggio(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 1f, TRILL3.in(3), 2, 1).build();
    public static final SFX PICTIONARY_OTHER_PLAYER_GUESSED = SFX.builder()
            .sound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.6f, 1.5f)
            .note(SoundEvents.BLOCK_NOTE_BLOCK_BASS, 0.5f, 8)
            .delay(1)
            .sound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 0.9f, 1.75f).build();
    public static final SFX PICTIONARY_WORD_REVEAL = SFX.builder()
            .sound(SoundEvents.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1.5f, 0.75f)
            .note(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 0.4f, 1.059f, 20).build();
    public static final SFX PICTIONARY_NEW_ROUND = SFX.builder()
            .sound(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, 1f)
            .sound(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, 1f, 0.5f).build();
    public static final SFX PICTIONARY_START_GUESSING = SFX.builder()
            .note(SoundEvents.GOAT_HORN_SOUNDS.get(0), 0.7f, BDUtil.fSharp(6), 3)
            .sound(SoundEvents.BLOCK_BEACON_ACTIVATE, 1f, 0.7f)
            .build();

    public static final SFX TELEPHONE_START = SFX.builder()
            .sound(SoundEvents.BLOCK_BEACON_ACTIVATE, 1f, 1f)
            .note(SoundEvents.GOAT_HORN_SOUNDS.get(1), 0.7f, BDUtil.fSharp(6), 8)
            .build();
    public static final SFX TELEPHONE_GUESS = PICTIONARY_START_GUESSING;
    public static final SFX TELEPHONE_BUILD = SFX.builder()
            .sound(SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR, 1f, 0.75f)
            .sound(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL, 1f, 0.5f).build();
    public static final SFX TELEPHONE_GALLERY_OPEN = MINI_FANFARE;
    public static final SFX TELEPHONE_GALLERY_REVEAL_PROMPT = SFX.builder()
            .sound(SoundEvents.ENTITY_ILLUSIONER_AMBIENT, 0.8f, 1.25f)
            .sound(SoundEvents.ENTITY_EVOKER_AMBIENT, 0.7f, 1.25f)
            .arpeggio(SoundEvents.BLOCK_WOODEN_BUTTON_CLICK_ON, 1f, Chord.MAJ.in(8), 2, 1).build();
    public static final SFX TELEPHONE_GALLERY_FLIP = CLICK;

    public static final SFX VERSUS_BUILD = TELEPHONE_BUILD;
    public static final SFX VERSUS_VOTE = MINI_FANFARE;
    public static final SFX VERSUS_VOTE_REVEAL_BUILD = SFX.builder()
            .sound(SoundEvents.BLOCK_BEACON_ACTIVATE, 1f, 1.7f).build();
    public static final SFX VERSUS_VOTE_START = PICTIONARY_START_GUESSING;
    public static final SFX VERSUS_VOTE_BEGIN_TALLY = SFX.builder()
            .arpeggio(SoundEvents.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 0.5f, Chord.MAJ7, 2, 1).build();
    public static final SFX VERSUS_VOTE_TALLY_EACH = SFX.builder()
            .sound(SoundEvents.BLOCK_TRIAL_SPAWNER_CLOSE_SHUTTER, 0.4f, 0.5f)
            .note(SoundEvents.BLOCK_NOTE_BLOCK_BELL, 0.2f, 5)
            .note(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, BDUtil.fSharp(6), -2).build();
    public static final SFX VERSUS_VOTE_RESULT = BUILD_REVEAL;

    public Playing play(ServerWorld world, Collection<PlayerRef> players) {
        return new Playing(world, players, new ArrayDeque<>(steps()));
    }

    public Playing play(ServerWorld world) {
        return play(world, world.getPlayers().stream().map(PlayerRef::of).collect(Collectors.toList()));
    }

    public Playing play(ServerWorld world, PlayerRef player) {
        return play(world, List.of(player));
    }

    public Playing play(ServerWorld world, Collection<PlayerRef> players, float transpose) {
        return new Playing(world, players, steps().stream().map(s -> s.transpose(transpose))
                .collect(ArrayDeque::new, ArrayDeque::addLast, ArrayDeque::addAll));
    }

    public Playing play(ServerWorld world, float transpose) {
        return play(world, world.getPlayers().stream().map(PlayerRef::of).collect(Collectors.toList()), transpose);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SFX poly(SFX... voices) {
        var steps = new ArrayDeque<Step>();

        var currentSteps = new ArrayDeque[voices.length];
        for (int i = 0; i < voices.length; i++) {
            currentSteps[i] = new ArrayDeque<>(voices[i].steps());
        }

        boolean end = false;
        int[] waiting = new int[voices.length];
        while (!end) {
            int lowestWaitTimeIndex = -1;
            int lowestWaitTime = Integer.MAX_VALUE;

            for (int i = 0; i < voices.length; i++) {
                if (waiting[i] > 0 && waiting[i] < lowestWaitTime) {
                    lowestWaitTime = waiting[i];
                    lowestWaitTimeIndex = i;
                    continue;
                }

                if (currentSteps[i].isEmpty()) {
                    continue;
                }

                var obj = currentSteps[i].removeFirst();
                if (obj instanceof SoundStep sound) {
                    steps.addLast(sound);
                } else if (obj instanceof WaitStep wait) {
                    waiting[i] = wait.delayTicks();
                }
            }

            if (lowestWaitTimeIndex >= 0) {
                for (int i = 0; i < voices.length; i++) waiting[i] -= lowestWaitTime;
                steps.addLast(new WaitStep(lowestWaitTime));
            }

            end = true;
            for (var dq : currentSteps) {
                if (!dq.isEmpty()) {
                    end = false;
                    break;
                }
            }
        }

        return new SFX(steps);
    }

    public static class Builder {
        private final Deque<Step> steps = new ArrayDeque<>();

        private Builder() {
        }

        public Builder sound(SoundEvent sound, float volume, float pitch) {
            this.steps.addLast(new SoundStep(sound, volume, pitch));
            return this;
        }

        public Builder sound(RegistryEntry.Reference<SoundEvent> sound, float volume, float pitch) {
            this.steps.addLast(new SoundStep(sound.value(), volume, pitch));
            return this;
        }

        public Builder note(SoundEvent sound, float volume, float fSharp, int note) {
            return sound(sound, volume, fSharp * NoteBlock.getNotePitch(note));
        }

        public Builder note(RegistryEntry.Reference<SoundEvent> sound, float volume, float fSharp, int note) {
            return note(sound.value(), volume, fSharp, note);
        }

        public Builder note(RegistryEntry.Reference<SoundEvent> sound, float volume, int note) {
            return note(sound, volume, 1f, note);
        }

        public Builder chord(RegistryEntry.Reference<SoundEvent> sound, float volume, float fSharp, Chord chord) {
            for (int n : chord.formula()) {
                note(sound, volume, fSharp, n);
            }
            return this;
        }

        public Builder chord(RegistryEntry.Reference<SoundEvent> sound, float volume, Chord chord) {
            return chord(sound, volume, 1f, chord);
        }

        public Builder arpeggio(SoundEvent sound, float volume, float fSharp, Chord chord, int timing, int repeat) {
            for (int i = 0; i < repeat; i++) for (int n : chord.formula()) {
                note(sound, volume, fSharp, n);
                delay(timing);
            }
            return this;
        }

        public Builder arpeggio(SoundEvent sound, float volume, Chord chord, int timing, int repeat) {
            return arpeggio(sound, volume, 1f, chord, timing, repeat);
        }

        public Builder arpeggio(RegistryEntry.Reference<SoundEvent> sound, float volume, Chord chord, int timing, int repeat) {
            return arpeggio(sound.value(), volume, 1f, chord, timing, repeat);
        }

        public Builder delay(int delayTicks) {
            this.steps.addLast(new WaitStep(delayTicks));
            return this;
        }

        public SFX build() {
            return new SFX(this.steps);
        }
    }

    public static class Playing implements TickingAnimation {
        private final ServerWorld world;
        private final Collection<PlayerRef> players;
        private final Deque<Step> steps;

        private int delay = 0;

        private Playing(ServerWorld world, Collection<PlayerRef> players, Deque<Step> steps) {
            this.world = world;
            this.players = players;
            this.steps = steps;
        }

        @Override
        public boolean tick(ServerWorld world) {
            if (steps.isEmpty()) {
                return false;
            }

            this.delay--;

            if (this.delay > 0) {
                return true;
            }

            while (this.delay <= 0 && !steps.isEmpty()) {
                this.delay = steps.removeFirst().exec(this.players, this.world);
            }

            return !steps.isEmpty();
        }
    }

    interface Step {
        int exec(Collection<PlayerRef> players, ServerWorld world);

        Step transpose(float interval);
    }

    record SoundStep(SoundEvent sound, float volume, float pitch) implements Step {
        @Override
        public int exec(Collection<PlayerRef> players, ServerWorld world) {
            final long seed = world.random.nextLong();
            for (var player : players) {
                player.ifOnline(world, s -> s.networkHandler.sendPacket(
                        new PlaySoundFromEntityS2CPacket(RegistryEntry.of(sound), SoundCategory.MASTER, s, volume(), pitch, seed)
                ));
            }

            return 0;
        }

        @Override
        public Step transpose(float interval) {
            float mul = (float) Math.pow(2, interval / 12);
            return new SoundStep(sound(), volume(), pitch() * mul);
        }
    }

    record WaitStep(int delayTicks) implements Step {
        @Override
        public int exec(Collection<PlayerRef> players, ServerWorld world) {
            return delayTicks();
        }

        @Override
        public Step transpose(float interval) {
            return this;
        }
    }
}
