package io.github.foundationgames.builderdash.tools.item;

import eu.pb4.polymer.core.api.item.PolymerItem;
import io.github.foundationgames.builderdash.tools.BDToolsState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Consumer;

public class InstantOperationItem extends Item implements PolymerItem {
    public static final Identifier BLAZE_ROD_MODEL = Identifier.of("blaze_rod");
    public static final Identifier BREEZE_ROD_MODEL = Identifier.of("breeze_rod");

    private final Identifier model;
    private final Consumer<BDToolsState> operation;

    public InstantOperationItem(Settings settings, Identifier model, Consumer<BDToolsState> operation) {
        super(settings.rarity(Rarity.UNCOMMON)
                .maxCount(1));
        this.model = model;
        this.operation = operation;
    }

    public static InstantOperationItem undo(Settings settings) {
        return new InstantOperationItem(settings, BLAZE_ROD_MODEL, BDToolsState::undo);
    }

    public static InstantOperationItem redo(Settings settings) {
        return new InstantOperationItem(settings, BREEZE_ROD_MODEL, BDToolsState::redo);
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

    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            var tools = BDToolsState.get(player);
            operation.accept(tools);

            return ActionResult.SUCCESS;
        }

        return super.use(world, user, hand);
    }
}