package io.github.foundationgames.builderdash.tools;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AuditLog {
    public final int maxUndos;
    private final List<Audit> audits = new ArrayList<>();
    private int cursor = -1;

    public AuditLog(int maxUndos) {
        this.maxUndos = maxUndos;
    }

    public Audit audit(World world, Consumer<AuditBuilder> builder, int[] blocksChanged) {
        var ab = new AuditBuilder(world);
        builder.accept(ab);
        var audit = ab.build();

        while (audits.size() - 1 > cursor) {
            audits.removeLast();
        }

        audit.apply(false, blocksChanged);
        audits.add(audit);
        cursor++;
        this.normalize();

        return audit;
    }

    public boolean undo(int[] blocksChanged) {
        if (cursor < 0) {
            return false;
        }

        var audit = audits.get(cursor);
        audit.apply(true, blocksChanged);
        cursor--;
        this.normalize();

        return true;
    }

    public boolean redo(int[] blocksChanged) {
        if (cursor == audits.size() - 1) {
            return false;
        }

        cursor++;
        var audit = audits.get(cursor);
        audit.apply(false, blocksChanged);
        this.normalize();

        return true;
    }

    private void normalize() {
        while (audits.size() > this.maxUndos) {
            audits.removeFirst();
            cursor--;
        }

        cursor = MathHelper.clamp(cursor, -1, audits.size() - 1);
    }

    // Block Palette: Maps block states used in this audit to integer indices
    // Conversion Palette: List of 64 bit ints, each corresponding to a unique block state to block state conversion.
    //                     The first 32 bits are the index of <blockPalette> of the original block, the last 32 bits are
    //                     the index of the new changed block
    // Modifications: A mapping of long-packed BlockPos positions to conversionPalette indices
    public record Audit(World world, List<BlockState> blockPalette, LongList conversionPalette, Long2IntMap modifications) {
        public void apply(boolean undo, int[] blocksChanged) {
            var pos = new BlockPos.Mutable();
            int changed = 0;
            for (var e : modifications().long2IntEntrySet()) {
                pos.set(e.getLongKey());
                long conv = conversionPalette().getLong(e.getIntValue());

                int sID;
                if (undo) {
                    sID = (int) (conv >> 32); // old
                } else {
                    sID = (int) conv; // new
                }

                world().setBlockState(pos, blockPalette().get(sID), 3, 0);
                changed++;
            }

            if (blocksChanged != null && blocksChanged.length > 0) {
                blocksChanged[0] = changed;
            }
        }
    }

    public static class AuditBuilder {
        private final World world;

        private final List<BlockState> blockPalette = new ArrayList<>();
        private final LongList convPalette = new LongArrayList();
        private final Long2IntMap modifications = new Long2IntOpenHashMap();

        public AuditBuilder(World world) {
            this.world = world;
        }

        private int idFor(BlockState state) {
            int id = this.blockPalette.indexOf(state);
            if (id < 0) {
                id = this.blockPalette.size();
                this.blockPalette.add(state);
            }
            return id;
        }

        public AuditBuilder setBlockState(BlockPos pos, BlockState state) {
            int oldS = idFor(world.getBlockState(pos));
            int newS = idFor(state);

            if (oldS == newS) {
                return this;
            }

            long conv = newS | ((long)oldS << 32);
            int convId = this.convPalette.indexOf(conv);
            if (convId < 0) {
                convId = this.convPalette.size();
                this.convPalette.add(conv);
            }

            this.modifications.put(pos.asLong(), convId);
            return this;
        }

        public Audit build() {
            return new Audit(this.world, this.blockPalette, this.convPalette, this.modifications);
        }
    }
}
