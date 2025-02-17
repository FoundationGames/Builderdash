package io.github.foundationgames.builderdash.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.api.game.world.generator.TemplateChunkGenerator;

import java.util.List;

public class BuilderdashMap {
    private final MapTemplate template;
    private final BuilderdashMapConfig config;
    public final BlockBounds spawn;
    public final BlockPos buildZonesStart;
    public final BuildZone privateZoneTemplate;
    public final BuildZone singleZone;
    public final BuildZone doubleZone;
    public final Vec3d titlePos;

    public List<BuildZone> cachedPrivateZones;

    public BuilderdashMap(MapTemplate template, BuilderdashMapConfig config, BlockBounds spawn, BlockPos buildZonesStart, BuildZone privateZoneTemplate, BuildZone singleZone, BuildZone doubleZone, Vec3d titlePos) {
        this.template = template;
        this.config = config;
        this.spawn = spawn;
        this.buildZonesStart = buildZonesStart;
        this.privateZoneTemplate = privateZoneTemplate;
        this.singleZone = singleZone;
        this.doubleZone = doubleZone;
        this.titlePos = titlePos;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
