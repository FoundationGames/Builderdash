package io.github.foundationgames.builderdash.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.foundationgames.builderdash.BDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.game.GameOpenException;

import java.io.IOException;

public record BuilderdashMapConfig(int time, Identifier mapId) {
    public static final Codec<BuilderdashMapConfig> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.INT.optionalFieldOf("time", 6000).forGetter(BuilderdashMapConfig::time),
                    Identifier.CODEC.fieldOf("map_id").forGetter(BuilderdashMapConfig::mapId)
            ).apply(inst, BuilderdashMapConfig::new)
    );

    public static final String[] SINGLE_DISPLAY = {"display_singlezone"};
    public static final String[] DOUBLE_DISPLAY = {"display_doublezone_1", "display_doublezone_2"};

    public BuilderdashMap buildMap(MinecraftServer server) throws GameOpenException {
        MapTemplate template;
        try {
            template = MapTemplateSerializer.loadFromResource(server, mapId());
        } catch (IOException ex) {
            throw new GameOpenException(Text.literal(String.format("Failed to load map %s", mapId())));
        }

        var spawnRegion = BDUtil.regionOrThrow(mapId(), template, "spawn");
        var buildZonesStart = BDUtil.regionOrThrow(mapId(), template, "build_zones_start");

        var privateZoneTemplate = BuildZone.get(mapId(), template, "privatezone", new String[0]);
        var singleZone = BuildZone.get(mapId(), template, "singlezone", SINGLE_DISPLAY);
        var doubleZone = BuildZone.get(mapId(), template, "doublezone", DOUBLE_DISPLAY);

        return new BuilderdashMap(template, this, spawnRegion.getBounds(), buildZonesStart.getBounds().min(), privateZoneTemplate, singleZone, doubleZone);
    }
}
