package io.github.foundationgames.builderdash.game.map;

import io.github.foundationgames.builderdash.BDUtil;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.Set;
import java.util.UUID;

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

    public void copyBuildSliceWithEntities(ServerWorld world, BlockPos to, int slice) {
        slice = MathHelper.clamp(slice, 0, this.buildSafeArea.size().getY() - 1);

        var offset = to.subtract(buildSafeArea().min());

        var srcPos = new BlockPos.Mutable();
        var destPos = new BlockPos.Mutable();

        var srcMinPos = new BlockPos(buildSafeArea().min().getX(), buildSafeArea().min().getY() + slice, buildSafeArea().min().getZ());
        var srcMaxPos = new BlockPos(buildSafeArea().max().getX(), buildSafeArea().min().getY() + slice, buildSafeArea().max().getZ());

        var iter = new CuboidBlockIterator(srcMinPos.getX(), srcMinPos.getY(), srcMinPos.getZ(),
                srcMaxPos.getX(), srcMaxPos.getY(), srcMaxPos.getZ());

        while (iter.step()) {
            srcPos.set(iter.getX(), iter.getY(), iter.getZ());
            destPos.set(iter.getX() + offset.getX(), iter.getY() + offset.getY(), iter.getZ() + offset.getZ());

            var state = world.getBlockState(srcPos);
            world.setBlockState(destPos, state, 3, 0);

            var be = world.getBlockEntity(srcPos);
            if (be != null && state.getBlock() instanceof BlockEntityProvider beBlock) {
                var newBe = beBlock.createBlockEntity(destPos, state);
                if (newBe != null) {
                    var nbt = be.createNbt(world.getRegistryManager());
                    newBe.read(nbt, world.getRegistryManager());

                    world.addBlockEntity(newBe);
                }
            }
        }

        var offsetF = Vec3d.of(offset);
        var srcMin = Vec3d.of(srcMinPos);
        var srcMax = Vec3d.of(srcMaxPos).add(1, 1, 1);
        var dstMin = srcMin.add(offsetF);
        var dstMax = srcMax.add(offsetF);

        // Delete old entities
        var entities = world.getOtherEntities(null, new Box(dstMin, dstMax));
        for (var entity : entities) if (!(entity instanceof PlayerEntity)) {
            entity.teleport(world, 0, -9999, 0, Set.of(), 0, 0, true);
            entity.remove(Entity.RemovalReason.KILLED);
        }

        // Add new copied ones
        entities = world.getOtherEntities(null, new Box(srcMin, srcMax));
        for (var entity : entities) if (!(entity instanceof PlayerEntity)) {
            var nbt = new NbtCompound();
            entity.writeNbt(nbt);

            var newEntity = entity.getType().create(world, SpawnReason.COMMAND);
            if (newEntity != null) {
                newEntity.readNbt(nbt);
                newEntity.setUuid(UUID.randomUUID());
                newEntity.setPosition(entity.getPos().add(offsetF));
                world.spawnEntity(newEntity);
            }
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

        var offsetF = Vec3d.of(offset);
        var dstMin = Vec3d.of(buildSafeArea().min()).add(offsetF);
        var dstMax = Vec3d.of(buildSafeArea().max()).add(offsetF).add(1, 1, 1);

        var entities = world.getOtherEntities(null, new Box(dstMin, dstMax));
        for (var entity : entities) if (!(entity instanceof PlayerEntity)) {
            entity.teleport(world, 0, -9999, 0, Set.of(), 0, 0, true);
            entity.remove(Entity.RemovalReason.KILLED);
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
                this.zone.copyBuildSliceWithEntities(world, this.dest, this.currentSlice);
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
