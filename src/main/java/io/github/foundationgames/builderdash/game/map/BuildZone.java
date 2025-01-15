package io.github.foundationgames.builderdash.game.map;

import io.github.foundationgames.builderdash.BDUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

public record BuildZone(BlockBounds templateArea, BlockBounds playerSafeArea, BlockBounds buildSafeArea) {
    public static BuildZone get(Identifier mapId, MapTemplate template, String marker) {
        var templateRegion = template.getMetadata().getFirstRegion(marker + "_template");
        var playerSafeRegion = BDUtil.regionOrThrow(mapId, template, marker + "_playersafe");
        var buildSafeRegion = BDUtil.regionOrThrow(mapId, template, marker + "_buildsafe");

        var templateArea = BlockBounds.of(BlockPos.ORIGIN, BlockPos.ORIGIN);
        if (templateRegion != null) {
            templateArea = templateRegion.getBounds();
        }

        return new BuildZone(templateArea, playerSafeRegion.getBounds(), buildSafeRegion.getBounds());
    }

    public BuildZone copy(ServerWorld world, BlockPos to) {
        var offset = to.subtract(templateArea().min());

        var srcPos = new BlockPos.Mutable();
        var destPos = new BlockPos.Mutable();

        var iter = new CuboidBlockIterator(templateArea().min().getX(), templateArea().min().getY(), templateArea().min().getZ(),
                templateArea().max().getX(), templateArea().max().getY(), templateArea().max().getZ());

        while (iter.step()) {
            srcPos.set(iter.getX(), iter.getY(), iter.getZ());
            destPos.set(iter.getX() + offset.getX(), iter.getY() + offset.getY(), iter.getZ() + offset.getZ());

            world.setBlockState(destPos, world.getBlockState(srcPos), 3, 0);
        }

        return new BuildZone(
                templateArea().offset(offset),
                playerSafeArea().offset(offset),
                buildSafeArea().offset(offset));
    }

    public void copyBuildSlice(ServerWorld world, BlockPos to, int slice) {
        slice = MathHelper.clamp(slice, 0, this.buildSafeArea.size().getY() - 1);

        var offset = to.subtract(buildSafeArea().min());

        var srcPos = new BlockPos.Mutable();
        var destPos = new BlockPos.Mutable();

        var iter = new CuboidBlockIterator(buildSafeArea().min().getX(), buildSafeArea().min().getY() + slice, buildSafeArea().min().getZ(),
                buildSafeArea().max().getX(), buildSafeArea().min().getY() + slice, buildSafeArea().max().getZ());

        while (iter.step()) {
            srcPos.set(iter.getX(), iter.getY(), iter.getZ());
            destPos.set(iter.getX() + offset.getX(), iter.getY() + offset.getY(), iter.getZ() + offset.getZ());

            world.setBlockState(destPos, world.getBlockState(srcPos), 3, 0);
        }
    }

    public void copyBuild(ServerWorld world, BlockPos to) {
        var offset = to.subtract(buildSafeArea().min());

        var srcPos = new BlockPos.Mutable();
        var destPos = new BlockPos.Mutable();

        var iter = new CuboidBlockIterator(buildSafeArea().min().getX(), buildSafeArea().min().getY(), buildSafeArea().min().getZ(),
                buildSafeArea().max().getX(), buildSafeArea().max().getY(), buildSafeArea().max().getZ());

        while (iter.step()) {
            srcPos.set(iter.getX(), iter.getY(), iter.getZ());
            destPos.set(iter.getX() + offset.getX(), iter.getY() + offset.getY(), iter.getZ() + offset.getZ());

            world.setBlockState(destPos, world.getBlockState(srcPos), 3, 0);
        }
    }

    public AnimatedCopy makeCopyAnimation(BuildZone dest, boolean reverse, int ticksPerSlice) {
        return new AnimatedCopy(this, dest.buildSafeArea().min(), reverse, ticksPerSlice);
    }

    public static class AnimatedCopy {
        public final BuildZone zone;
        public final BlockPos dest;
        public final boolean reverse;
        public final int ticksPerSlice;

        private int currentSlice = -1;
        private int timeToNextSlice = 0;

        public AnimatedCopy(BuildZone zone, BlockPos dest, boolean reverse, int ticksPerSlice) {
            this.zone = zone;
            this.dest = dest;
            this.reverse = reverse;
            this.ticksPerSlice = ticksPerSlice;

            if (this.reverse) {
                this.currentSlice = zone.buildSafeArea().size().getY();
            }
        }

        public boolean tick(ServerWorld world) {
            if (this.timeToNextSlice <= 0) {
                if (this.reverse) {
                    this.currentSlice--;
                } else {
                    this.currentSlice++;
                }

                this.timeToNextSlice = this.ticksPerSlice;
                this.zone.copyBuildSlice(world, this.dest, this.currentSlice);
            }

            this.timeToNextSlice--;

            if (this.reverse) {
                return this.currentSlice != 0;
            } else {
                return this.currentSlice != this.zone.buildSafeArea().size().getY();
            }
        }
    }
}
