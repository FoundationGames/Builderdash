package io.github.foundationgames.builderdash.game.element;

import net.minecraft.server.world.ServerWorld;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface TickingAnimation {
    boolean tick(ServerWorld world);

    static TickingAnimation instant(Consumer<ServerWorld> func) {
        return world -> {
            func.accept(world);
            return false;
        };
    }

    static TickingAnimation wait(int time) {
        return new Wait(time);
    }

    static TickingAnimation sequence(TickingAnimation... anims) {
        return new Sequence(new ArrayDeque<>(List.of(anims)));
    }

    static TickingAnimation pool(TickingAnimation... anims) {
        return new Pool(new HashSet<>(List.of(anims)));
    }

    class Wait implements TickingAnimation {
        private int time;

        public Wait(int time) {
            this.time = time;
        }

        @Override
        public boolean tick(ServerWorld world) {
            this.time--;
            return this.time > 0;
        }
    }

    class Sequence implements TickingAnimation {
        public final Deque<TickingAnimation> anims;

        public Sequence(Deque<TickingAnimation> anims) {
            this.anims = anims;
        }

        @Override
        public boolean tick(ServerWorld world) {
            if (this.anims.isEmpty()) {
                return false;
            }

            boolean r = this.anims.getFirst().tick(world);
            if (!r) {
                this.anims.removeFirst();
            }
            return !this.anims.isEmpty();
        }
    }

    class Pool implements TickingAnimation {
        public final Set<TickingAnimation> anims;

        public Pool(Set<TickingAnimation> anims) {
            this.anims = anims;
        }

        @Override
        public boolean tick(ServerWorld world) {
            var remove = new HashSet<TickingAnimation>();
            for (var anim : this.anims) {
                if (!anim.tick(world)) {
                    remove.add(anim);
                }
            }
            this.anims.removeAll(remove);

            return !anims.isEmpty();
        }
    }
}
