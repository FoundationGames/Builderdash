package io.github.foundationgames.builderdash.game.map;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class PrivateBuildZoneManager {
    public final ServerWorld world;
    public final BuildZone template;
    public final BlockPos start;
    public final int maxRowSize;

    public List<BuildZone> cachedBuildZones = new ArrayList<>();
    public List<BuildZone> buildZones = new ArrayList<>();

    public PrivateBuildZoneManager(ServerWorld world, BuildZone template, BlockPos start, int maxRowSize) {
        this.world = world;
        this.template = template;
        this.start = start;
        this.maxRowSize = maxRowSize;
    }

    public void preallocateBuildZones(int count) {
        for (int i = 0; i < count; i++) {
            this.cachedBuildZones.add(createNewBuildZone(this.cachedBuildZones.size()));
        }
    }

    public BuildZone requestNewBuildZone() {
        BuildZone newBuildZone;
        if (this.buildZones.size() < this.cachedBuildZones.size()) {
            newBuildZone = this.cachedBuildZones.get(this.buildZones.size());
        } else {
            newBuildZone = this.createNewBuildZone(this.cachedBuildZones.size());
        }
        this.buildZones.add(newBuildZone);
        this.cachedBuildZones.add(newBuildZone);

        return newBuildZone;
    }

    private BuildZone createNewBuildZone(int index) {
        int gridZ = index / this.maxRowSize;
        int gridX = index % this.maxRowSize;

        var cellSize = this.template.templateArea().size();

        int worldX = start.getX() + (cellSize.getX() + 1) * gridX;
        int worldY = start.getY();
        int worldZ = start.getZ() + (cellSize.getZ() + 1) * gridZ;

        return this.template.copy(this.world, new BlockPos(worldX, worldY, worldZ));
    }
}
