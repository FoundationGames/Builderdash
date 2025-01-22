package io.github.foundationgames.builderdash.tools.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.BiConsumer;

public class DistantOperationItem extends Item implements PolymerItem {
    public static final Text BRUSH = Text.translatable("tooltip.builderdash.tool.brush").formatted(Formatting.GRAY);

    public static final Identifier SNOWBALL_MODEL = Identifier.of("snowball");
    public static final Identifier SLIME_MODEL = Identifier.of("slime_ball");
    public static final Identifier MAGMA_MODEL = Identifier.of("magma_cream");

    private final Identifier model;
    private final BiConsumer<BDToolsState, BlockPos> operation;

    public DistantOperationItem(Settings settings, Identifier model, BiConsumer<BDToolsState, BlockPos> operation) {
        super(settings.rarity(Rarity.RARE)
                .maxCount(1));
        this.model = model;
        this.operation = operation;
    }

    public static DistantOperationItem smallBrush(Settings settings) {
        return new DistantOperationItem(settings, SNOWBALL_MODEL, (t, p) -> t.brush(p, 2));
    }

    public static DistantOperationItem medBrush(Settings settings) {
        return new DistantOperationItem(settings, SLIME_MODEL, (t, p) -> t.brush(p, 4));
    }

    public static DistantOperationItem largeBrush(Settings settings) {
        return new DistantOperationItem(settings, MAGMA_MODEL, (t, p) -> t.brush(p, 6));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.SNOWBALL;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.model;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        PolymerItem.super.modifyClientTooltip(tooltip, stack, context);

        tooltip.add(AreaOperationItem.TOOL);
        tooltip.add(BRUSH);
    }

    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            var tools = BDToolsState.get(player);
            var hit = player.raycast(64, 0, false);
            var os = player.getRotationVector().multiply(0.01);

            operation.accept(tools, BlockPos.ofFloored(hit.getPos().add(os)));

            return ActionResult.SUCCESS;
        }

        return super.use(world, user, hand);
    }
}
