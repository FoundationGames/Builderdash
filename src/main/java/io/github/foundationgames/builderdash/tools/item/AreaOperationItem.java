package io.github.foundationgames.builderdash.tools.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.BlockBounds;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class AreaOperationItem extends Item implements PolymerItem {
    public static final Component TOOL = Component.translatable("tooltip.builderdash.tool.tool").withStyle(ChatFormatting.GRAY);
    public static final Component FILL = Component.translatable("tooltip.builderdash.tool.fill").withStyle(ChatFormatting.GRAY);

    public static final ResourceLocation SHULKER_MODEL = ResourceLocation.parse("shulker_shell");
    public static final ResourceLocation HEART_MODEL = ResourceLocation.parse("heart_of_the_sea");
    public static final ResourceLocation GUNPOWDER_MODEL = ResourceLocation.parse("gunpowder");
    public static final ResourceLocation COOKIE_MODEL = ResourceLocation.parse("cookie");

    private final BlockState selectionBox;
    private final ResourceLocation model;
    private final BiConsumer<BDToolsState, BlockBounds> operation;

    public AreaOperationItem(Properties settings, BlockState selectionBox, ResourceLocation model, BiConsumer<BDToolsState, BlockBounds> operation) {
        super(settings.rarity(Rarity.EPIC)
                .stacksTo(1));
        this.selectionBox = selectionBox;
        this.model = model;
        this.operation = operation;
    }

    public static AreaOperationItem boxFill(Properties settings) {
        return new AreaOperationItem(settings, Blocks.MAGENTA_STAINED_GLASS.defaultBlockState(), SHULKER_MODEL, BDToolsState::fill);
    }

    public static AreaOperationItem sphereFill(Properties settings) {
        return new AreaOperationItem(settings, Blocks.CYAN_STAINED_GLASS.defaultBlockState(), HEART_MODEL, BDToolsState::sphere);
    }

    public static AreaOperationItem cylinderFill(Properties settings) {
        return new AreaOperationItem(settings, Blocks.ORANGE_STAINED_GLASS.defaultBlockState(), COOKIE_MODEL, BDToolsState::cylinder);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.TRIDENT;
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

        tooltip.add(TOOL);
        tooltip.add(FILL);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        user.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (user instanceof ServerPlayer player) {
            var tools = BDToolsState.get(player);
            var hit = player.pick(32, 0, false);
            var os = player.getLookAngle().scale(0.01);

            tools.tickSelecting(BlockPos.containing(hit.getLocation().add(os)));
            tools.updateSelectionDisplay(world, this.selectionBox);
        }

        super.onUseTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof ServerPlayer player) {
            var tools = BDToolsState.get(player);
            var hit = player.pick(32, 0, false);
            var os = player.getLookAngle().scale(0.01);

            var sel = tools.endSelection(BlockPos.containing(hit.getLocation().add(os)));
            operation.accept(tools, sel);

            return true;
        }

        return super.releaseUsing(stack, world, user, remainingUseTicks);
    }
}
