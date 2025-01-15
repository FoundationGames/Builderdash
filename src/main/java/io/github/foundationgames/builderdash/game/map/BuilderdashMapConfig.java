package io.github.foundationgames.builderdash.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.foundationgames.builderdash.BDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

import java.io.IOException;

public record BuilderdashMapConfig(int time, Identifier mapId) {
    public static final Codec<BuilderdashMapConfig> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.INT.optionalFieldOf("time", 6000).forGetter(BuilderdashMapConfig::time),
                    Identifier.CODEC.fieldOf("map_id").forGetter(BuilderdashMapConfig::mapId)
            ).apply(inst, BuilderdashMapConfig::new)
    );

    public BuilderdashMap buildMap(MinecraftServer server) throws GameOpenException {
        MapTemplate template;
        try {
            template = MapTemplateSerializer.loadFromResource(server, mapId());
        } catch (IOException ex) {
            throw new GameOpenException(Text.literal(String.format("Failed to load map %s", mapId())));
        }

        var spawnRegion = BDUtil.regionOrThrow(mapId(), template, "spawn");
        var buildZonesStart = BDUtil.regionOrThrow(mapId(), template, "build_zones_start");

        var privateZoneTemplate = BuildZone.get(mapId(), template, "privatezone");
        var singleZone = BuildZone.get(mapId(), template, "singlezone");
        var doubleZone = BuildZone.get(mapId(), template, "doublezone");

        return new BuilderdashMap(template, this, spawnRegion.getBounds(), buildZonesStart.getBounds().min(), privateZoneTemplate, singleZone, doubleZone);
    }
}
