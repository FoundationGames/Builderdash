package io.github.foundationgames.builderdash.game.map;

import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.game.element.TickingAnimation;
import io.github.foundationgames.builderdash.game.element.display.InWorldDisplay;
import io.github.foundationgames.builderdash.game.sound.SFX;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record BuildZone(BlockBounds templateArea, BlockBounds playerSafeArea, BlockBounds buildSafeArea, InWorldDisplay[] displays) {
    public static BuildZone get(ResourceLocation mapId, MapTemplate template, String marker, String[] displays) {
        var templateRegion = template.getMetadata().getFirstRegion(marker + "_template");
        var playerSafeRegion = BDUtil.regionOrThrow(mapId, template, marker + "_playersafe");
        var buildSafeRegion = BDUtil.regionOrThrow(mapId, template, marker + "_buildsafe");

        var templateArea = BlockBounds.of(BlockPos.ZERO, BlockPos.ZERO);
        if (templateRegion != null) {
            templateArea = templateRegion.getBounds();
        }

        var iwDisplays = new InWorldDisplay[displays.length];

        for (int i = 0; i < displays.length; i++) {
            iwDisplays[i] = InWorldDisplay.of(BDUtil.regionOrThrow(mapId, template, displays[i]));
        }

        return new BuildZone(templateArea, playerSafeRegion.getBounds(), buildSafeRegion.getBounds(), iwDisplays);
    }

    public BuildZone copy(ServerLevel world, BlockPos to) {
        var offset = to.subtract(templateArea().min());

        var srcPos = new BlockPos.MutableBlockPos();
        var destPos = new BlockPos.MutableBlockPos();

        var iter = new Cursor3D(templateArea().min().getX(), templateArea().min().getY(), templateArea().min().getZ(),
                templateArea().max().getX(), templateArea().max().getY(), templateArea().max().getZ());

        while (iter.advance()) {
            srcPos.set(iter.nextX(), iter.nextY(), iter.nextZ());
            destPos.set(iter.nextX() + offset.getX(), iter.nextY() + offset.getY(), iter.nextZ() + offset.getZ());

            world.setBlock(destPos, world.getBlockState(srcPos), 3, 0);
        }

        for (var player : world.players()) {
            player.connection.chunkSender.sendNextChunks(player);
        }

        return new BuildZone(
                templateArea().offset(offset),
                playerSafeArea().offset(offset),
                buildSafeArea().offset(offset), InWorldDisplay.offset(offset, displays()));
    }

    public boolean copyBuildSliceWithEntities(ServerLevel world, BlockPos to, int slice) {
        slice = Mth.clamp(slice, 0, this.buildSafeArea.size().getY() - 1);

        var offset = to.subtract(buildSafeArea().min());

        var srcPos = new BlockPos.MutableBlockPos();
        var destPos = new BlockPos.MutableBlockPos();

        var srcMinPos = new BlockPos(buildSafeArea().min().getX(), buildSafeArea().min().getY() + slice, buildSafeArea().min().getZ());
        var srcMaxPos = new BlockPos(buildSafeArea().max().getX(), buildSafeArea().min().getY() + slice, buildSafeArea().max().getZ());

        var iter = new Cursor3D(srcMinPos.getX(), srcMinPos.getY(), srcMinPos.getZ(),
                srcMaxPos.getX(), srcMaxPos.getY(), srcMaxPos.getZ());

        boolean changed = false;
        while (iter.advance()) {
            srcPos.set(iter.nextX(), iter.nextY(), iter.nextZ());
            destPos.set(iter.nextX() + offset.getX(), iter.nextY() + offset.getY(), iter.nextZ() + offset.getZ());

            var state = world.getBlockState(srcPos);

            if (!changed) {
                changed = !state.is(world.getBlockState(destPos).getBlock());
            }

            world.setBlock(destPos, state, 3, 0);

            var be = world.getBlockEntity(srcPos);
            if (be != null && state.getBlock() instanceof EntityBlock beBlock) {
                var newBe = beBlock.newBlockEntity(destPos, state);
                if (newBe != null) {
                    var nbt = be.saveWithoutMetadata(world.registryAccess());
                    newBe.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), nbt));

                    world.setBlockEntity(newBe);
                }
            }
        }

        var offsetF = Vec3.atLowerCornerOf(offset);
        var srcMin = Vec3.atLowerCornerOf(srcMinPos);
        var srcMax = Vec3.atLowerCornerOf(srcMaxPos).add(1, 1, 1);
        var dstMin = srcMin.add(offsetF);
        var dstMax = srcMax.add(offsetF);

        // Delete old entities
        var entities = world.getEntities(null, new AABB(dstMin, dstMax));
        for (var entity : entities) if (!(entity instanceof Player)) {
            entity.teleportTo(world, 0, -9999, 0, Set.of(), 0, 0, true);
            entity.remove(Entity.RemovalReason.KILLED);

            changed = true;
        }

        // Add new copied ones
        entities = world.getEntities(null, new AABB(srcMin, srcMax));
        for (var entity : entities) if (!(entity instanceof Player)) {
            var data = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, world.registryAccess());
            entity.saveWithoutId(data);

            var newEntity = entity.getType().create(world, EntitySpawnReason.COMMAND);
            if (newEntity != null) {
                newEntity.load(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), data.buildResult()));
                newEntity.setUUID(UUID.randomUUID());
                newEntity.setPos(entity.position().add(offsetF));
                world.addFreshEntity(newEntity);
            }

            changed = true;
        }

        return changed;
    }

    public void copyBuild(ServerLevel world, BlockPos to) {
        var offset = to.subtract(buildSafeArea().min());

        var srcPos = new BlockPos.MutableBlockPos();
        var destPos = new BlockPos.MutableBlockPos();

        var iter = new Cursor3D(buildSafeArea().min().getX(), buildSafeArea().min().getY(), buildSafeArea().min().getZ(),
                buildSafeArea().max().getX(), buildSafeArea().max().getY(), buildSafeArea().max().getZ());

        while (iter.advance()) {
            srcPos.set(iter.nextX(), iter.nextY(), iter.nextZ());
            destPos.set(iter.nextX() + offset.getX(), iter.nextY() + offset.getY(), iter.nextZ() + offset.getZ());

            world.setBlock(destPos, world.getBlockState(srcPos), 3, 0);
        }

        var offsetF = Vec3.atLowerCornerOf(offset);
        var dstMin = Vec3.atLowerCornerOf(buildSafeArea().min()).add(offsetF);
        var dstMax = Vec3.atLowerCornerOf(buildSafeArea().max()).add(offsetF).add(1, 1, 1);

        var entities = world.getEntities(null, new AABB(dstMin, dstMax));
        for (var entity : entities) if (!(entity instanceof Player)) {
            entity.teleportTo(world, 0, -9999, 0, Set.of(), 0, 0, true);
            entity.remove(Entity.RemovalReason.KILLED);
        }
    }

    public CopyAnimation makeCopyAnimation(BuildZone dest, boolean reverse, int ticksPerSlice) {
        return new CopyAnimation(this, dest.buildSafeArea().min(), reverse, ticksPerSlice);
    }

    public static class CopyAnimation implements TickingAnimation {
        public final BuildZone zone;
        public final BlockPos dest;
        public final boolean reverse;
        public final int ticksPerSlice;

        private int currentSlice = -1;
        private int timeToNextSlice = 0;
        private final TickingAnimation.Pool soundPlayer = new Pool(new HashSet<>());

        private boolean active = true;

        public CopyAnimation(BuildZone zone, BlockPos dest, boolean reverse, int ticksPerSlice) {
            this.zone = zone;
            this.dest = dest;
            this.reverse = reverse;
            this.ticksPerSlice = ticksPerSlice;

            if (this.reverse) {
                this.currentSlice = zone.buildSafeArea().size().getY();
            }
        }

        @Override
        public boolean tick(ServerLevel world) {
            if (active) {
                int height = this.zone.buildSafeArea().size().getY();
                if (this.timeToNextSlice <= 0) {
                    if (this.reverse) {
                        while (this.currentSlice > 0) {
                            this.currentSlice--;

                            if (this.zone.copyBuildSliceWithEntities(world, this.dest, this.currentSlice)) {
                                float pitch = (float) this.currentSlice / height;
                                SFX.BUILD_LAYER.play(world, 12 * pitch).tick(world);
                                break;
                            }
                        }
                    } else {
                        while (this.currentSlice < height) {
                            this.currentSlice++;

                            if (this.zone.copyBuildSliceWithEntities(world, this.dest, this.currentSlice)) {
                                float pitch = (float) this.currentSlice / height;
                                SFX.BUILD_LAYER.play(world, 12 * pitch - 12).tick(world);
                                break;
                            }
                        }
                    }

                    this.timeToNextSlice = this.ticksPerSlice;
                }

                this.timeToNextSlice--;
            }

            if (this.reverse) {
                this.active = this.currentSlice != 0;
            } else {
                this.active = this.currentSlice != this.zone.buildSafeArea().size().getY();
            }

            return this.active || this.soundPlayer.tick(world);
        }
    }
}
