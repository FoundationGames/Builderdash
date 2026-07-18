package io.github.foundationgames.builderdash.tools.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class InstantOperationItem extends Item implements PolymerItem {
    public static final Identifier BLAZE_ROD_MODEL = Identifier.parse("blaze_rod");
    public static final Identifier BREEZE_ROD_MODEL = Identifier.parse("breeze_rod");

    private final Identifier model;
    private final Consumer<BDToolsState> operation;

    public InstantOperationItem(Properties settings, Identifier model, Consumer<BDToolsState> operation) {
        super(settings.rarity(Rarity.UNCOMMON)
                .stacksTo(1));
        this.model = model;
        this.operation = operation;
    }

    public static InstantOperationItem undo(Properties settings) {
        return new InstantOperationItem(settings, BLAZE_ROD_MODEL, BDToolsState::undo);
    }

    public static InstantOperationItem redo(Properties settings) {
        return new InstantOperationItem(settings, BREEZE_ROD_MODEL, BDToolsState::redo);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.SNOWBALL;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context, HolderLookup.Provider lookup) {
        return this.model;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user instanceof ServerPlayer player) {
            var tools = BDToolsState.get(player);
            operation.accept(tools);

            return InteractionResult.SUCCESS;
        }

        return super.use(world, user, hand);
    }
}