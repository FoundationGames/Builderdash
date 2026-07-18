package io.github.foundationgames.builderdash.tools;

import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.tools.item.AreaOperationItem;
import io.github.foundationgames.builderdash.tools.item.DistantOperationItem;
import io.github.foundationgames.builderdash.tools.item.InstantOperationItem;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class BDToolsItems {
    public static final InstantOperationItem UNDO = register(InstantOperationItem::undo, "tool_undo");
    public static final InstantOperationItem REDO = register(InstantOperationItem::redo, "tool_redo");
    public static final AreaOperationItem FILL = register(AreaOperationItem::boxFill, "tool_fill");
    public static final AreaOperationItem SPHERE = register(AreaOperationItem::sphereFill, "tool_sphere");
    public static final AreaOperationItem CYLINDER = register(AreaOperationItem::cylinderFill, "tool_cylinder");
    public static final DistantOperationItem BRUSH_SMALL = register(DistantOperationItem::smallBrush, "tool_brush_small");
    public static final DistantOperationItem BRUSH_MED = register(DistantOperationItem::medBrush, "tool_brush_med");
    public static final DistantOperationItem BRUSH_LARGE = register(DistantOperationItem::largeBrush, "tool_brush_large");

    public static <T extends Item> T register(Function<Item.Properties, T> item, String name) {
        var id = Builderdash.id(name);
        return Registry.register(BuiltInRegistries.ITEM, id,
                item.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
    }

    public static void init() {
    }
}
