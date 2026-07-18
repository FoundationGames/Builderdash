package io.github.foundationgames.builderdash.tools.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class DistantOperationItem extends Item implements PolymerItem {
    public static final Component BRUSH = Component.translatable("tooltip.builderdash.tool.brush").withStyle(ChatFormatting.GRAY);

    public static final ResourceLocation SNOWBALL_MODEL = ResourceLocation.parse("snowball");
    public static final ResourceLocation SLIME_MODEL = ResourceLocation.parse("slime_ball");
    public static final ResourceLocation MAGMA_MODEL = ResourceLocation.parse("magma_cream");

    private final ResourceLocation model;
    private final BiConsumer<BDToolsState, BlockPos> operation;

    public DistantOperationItem(Properties settings, ResourceLocation model, BiConsumer<BDToolsState, BlockPos> operation) {
        super(settings.rarity(Rarity.RARE)
                .stacksTo(1));
        this.model = model;
        this.operation = operation;
    }

    public static DistantOperationItem smallBrush(Properties settings) {
        return new DistantOperationItem(settings, SNOWBALL_MODEL, (t, p) -> t.brush(p, 2));
    }

    public static DistantOperationItem medBrush(Properties settings) {
        return new DistantOperationItem(settings, SLIME_MODEL, (t, p) -> t.brush(p, 4));
    }

    public static DistantOperationItem largeBrush(Properties settings) {
        return new DistantOperationItem(settings, MAGMA_MODEL, (t, p) -> t.brush(p, 6));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.SNOWBALL;
    }

    @Override
    public @Nullable ResourceLocation getPolymerItemModel(ItemStack stack, PacketContext context) {
        return this.model;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void modifyClientTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
        PolymerItem.super.modifyClientTooltip(tooltip, stack, context);

        tooltip.add(AreaOperationItem.TOOL);
        tooltip.add(BRUSH);
    }

    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user instanceof ServerPlayer player) {
            var tools = BDToolsState.get(player);
            var hit = player.pick(64, 0, false);
            var os = player.getLookAngle().scale(0.01);

            operation.accept(tools, BlockPos.containing(hit.getLocation().add(os)));

            return InteractionResult.SUCCESS;
        }

        return super.use(world, user, hand);
    }
}
