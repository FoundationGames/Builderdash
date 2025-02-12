package io.github.foundationgames.builderdash.tools.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.function.BiConsumer;

public class AreaOperationItem extends Item implements PolymerItem {
    public static final Text TOOL = Text.translatable("tooltip.builderdash.tool.tool").formatted(Formatting.GRAY);
    public static final Text FILL = Text.translatable("tooltip.builderdash.tool.fill").formatted(Formatting.GRAY);

    public static final Identifier SHULKER_MODEL = Identifier.of("shulker_shell");
    public static final Identifier HEART_MODEL = Identifier.of("heart_of_the_sea");
    public static final Identifier GUNPOWDER_MODEL = Identifier.of("gunpowder");
    public static final Identifier COOKIE_MODEL = Identifier.of("cookie");

    private final BlockState selectionBox;
    private final Identifier model;
    private final BiConsumer<BDToolsState, BlockBounds> operation;

    public AreaOperationItem(Settings settings, BlockState selectionBox, Identifier model, BiConsumer<BDToolsState, BlockBounds> operation) {
        super(settings.rarity(Rarity.EPIC)
                .maxCount(1));
        this.selectionBox = selectionBox;
        this.model = model;
        this.operation = operation;
    }

    public static AreaOperationItem boxFill(Settings settings) {
        return new AreaOperationItem(settings, Blocks.MAGENTA_STAINED_GLASS.getDefaultState(), SHULKER_MODEL, BDToolsState::fill);
    }

    public static AreaOperationItem sphereFill(Settings settings) {
        return new AreaOperationItem(settings, Blocks.CYAN_STAINED_GLASS.getDefaultState(), HEART_MODEL, BDToolsState::sphere);
    }

    public static AreaOperationItem cylinderFill(Settings settings) {
        return new AreaOperationItem(settings, Blocks.ORANGE_STAINED_GLASS.getDefaultState(), COOKIE_MODEL, BDToolsState::cylinder);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return Items.TRIDENT;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        PolymerItem.super.modifyClientTooltip(tooltip, stack, player);

        tooltip.add(TOOL);
        tooltip.add(FILL);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.SPEAR;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (user instanceof ServerPlayerEntity player) {
            var tools = BDToolsState.get(player);
            var hit = player.raycast(32, 0, false);
            var os = player.getRotationVector().multiply(0.01);

            tools.tickSelecting(BlockPos.ofFloored(hit.getPos().add(os)));
            tools.updateSelectionDisplay(world, this.selectionBox);
        }

        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof ServerPlayerEntity player) {
            var tools = BDToolsState.get(player);
            var hit = player.raycast(32, 0, false);
            var os = player.getRotationVector().multiply(0.01);

            var sel = tools.endSelection(BlockPos.ofFloored(hit.getPos().add(os)));
            operation.accept(tools, sel);

            return;
        }

        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }
}
