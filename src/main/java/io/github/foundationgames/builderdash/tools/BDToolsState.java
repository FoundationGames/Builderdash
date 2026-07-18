package io.github.foundationgames.builderdash.tools;

import com.mojang.math.Transformation;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.tools.ui.BDToolboxGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.util.PlayerRef;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BDToolsState {
    public static final String UNDO = "message.builderdash.tool.undo";
    public static final String REDO = "message.builderdash.tool.redo";
    public static final String OPERATION = "message.builderdash.tool.operation";
    public static final Component UNDO_FAIL = Component.translatable("message.builderdash.tool.undo_fail").withStyle(ChatFormatting.RED);
    public static final Component REDO_FAIL = Component.translatable("message.builderdash.tool.redo_fail").withStyle(ChatFormatting.RED);

    public static final int DEFAULT_MAX_UNDOS = 16;
    private static final Map<PlayerRef, Deque<BDToolsState>> PLAYERS = new HashMap<>();

    public final MinecraftServer server;
    public final PlayerRef player;
    public final AuditLog audits;
    public final @Nullable BlockBounds restriction;

    private BlockPos selectStart = null;
    private final BlockPos.MutableBlockPos selectEnd = new BlockPos.MutableBlockPos();

    private Display.BlockDisplay selectionDisplay = null;

    public BDToolsState(MinecraftServer server, PlayerRef player, int maxUndos, @Nullable BlockBounds restriction) {
        this.server = server;
        this.player = player;
        this.audits = new AuditLog(maxUndos);
        this.restriction = restriction;

        PLAYERS.computeIfAbsent(player, p -> new ArrayDeque<>()).add(this);
    }

    public boolean denyOperation() {
        return false;
    }

    public void openToolbox(ServerPlayer player) {
        if (denyOperation()) return;

        new BDToolboxGui(player).open();
    }

    public void undo() {
        if (denyOperation()) return;

        int[] blocksChanged = {0};
        boolean success = this.audits.undo(blocksChanged);

        var player = this.player.getEntity(this.server);
        if (player != null) {
            player.sendSystemMessage(success ? Component.translatable(UNDO, blocksChanged[0]).withStyle(ChatFormatting.AQUA)
                            : UNDO_FAIL, false);
        }
    }

    public void redo() {
        if (denyOperation()) return;

        int[] blocksChanged = {0};
        boolean success = this.audits.redo(blocksChanged);

        var player = this.player.getEntity(this.server);
        if (player != null) {
            player.sendSystemMessage(success ? Component.translatable(REDO, blocksChanged[0]).withStyle(ChatFormatting.AQUA)
                            : REDO_FAIL, false);
        }
    }

    public void fill(BlockBounds area) {
        if (denyOperation()) return;

        var player = this.player.getEntity(this.server);
        if (player != null) {
            var params = OperationParams.of(player.getInventory());
            var world = player.level();
            int[] blocksChanged = {0};

            this.audits.audit(world,
                    au -> {
                        for (var pos : BlockPos.betweenClosed(area.min(), area.max())) {
                            if (restriction != null && !restriction.contains(pos)) {
                                continue;
                            }
                            if (!params.canSet(world, pos)) {
                                continue;
                            }

                            au.setBlockState(pos, params.getBlock(world));
                        }
                    }, blocksChanged);

            player.sendSystemMessage(Component.translatable(OPERATION, blocksChanged[0])
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
    }

    public void sphere(BlockBounds area) {
        if (denyOperation()) return;

        var player = this.player.getEntity(this.server);
        if (player != null) {
            var params = OperationParams.of(player.getInventory());
            var world = player.level();
            int[] blocksChanged = {0};

            double xRad = area.size().getX() * 0.5 + 0.25;
            double yRad = area.size().getY() * 0.5 + 0.25;
            double zRad = area.size().getZ() * 0.5 + 0.25;

            this.audits.audit(world,
                    au -> {
                        var center = Vec3.atCenterOf(area.min()).add(Vec3.atCenterOf(area.max())).scale(0.5);
                        for (var pos : BlockPos.betweenClosed(area.min(), area.max())) {
                            if (restriction != null && !restriction.contains(pos)) {
                                continue;
                            }
                            if (!params.canSet(world, pos)) {
                                continue;
                            }

                            double lx = pos.getX() + 0.5 - center.x();
                            double ly = pos.getY() + 0.5 - center.y();
                            double lz = pos.getZ() + 0.5 - center.z();

                            double h = ((lx * lx) / (xRad * xRad)) + ((ly * ly) / (yRad * yRad)) + ((lz * lz) / (zRad * zRad));

                            if (h > 1) {
                                continue;
                            }

                            au.setBlockState(pos, params.getBlock(world));
                        }
                    }, blocksChanged);

            player.sendSystemMessage(Component.translatable(OPERATION, blocksChanged[0])
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
    }

    public void cylinder(BlockBounds area) {
        if (denyOperation()) return;

        var player = this.player.getEntity(this.server);
        if (player != null) {
            var params = OperationParams.of(player.getInventory());
            var world = player.level();
            int[] blocksChanged = {0};

            double xRad = area.size().getX() * 0.5 + 0.25;
            double zRad = area.size().getZ() * 0.5 + 0.25;

            this.audits.audit(world,
                    au -> {
                        var center = Vec3.atCenterOf(area.min()).add(Vec3.atCenterOf(area.max())).scale(0.5);
                        for (var pos : BlockPos.betweenClosed(area.min(), area.max())) {
                            if (restriction != null && !restriction.contains(pos)) {
                                continue;
                            }
                            if (!params.canSet(world, pos)) {
                                continue;
                            }

                            double lx = pos.getX() + 0.5 - center.x();
                            double lz = pos.getZ() + 0.5 - center.z();

                            double h = ((lx * lx) / (xRad * xRad)) + ((lz * lz) / (zRad * zRad));

                            if (h > 1) {
                                continue;
                            }

                            au.setBlockState(pos, params.getBlock(world));
                        }
                    }, blocksChanged);

            player.sendSystemMessage(Component.translatable(OPERATION, blocksChanged[0])
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false);
        }
    }

    public void brush(BlockPos origin, int radius) {
        if (denyOperation()) return;

        var player = this.player.getEntity(this.server);
        if (player != null) {
            var params = OperationParams.of(player.getInventory());
            var world = player.level();
            int[] blocksChanged = {0};

            var area = BlockBounds.of(
                    origin.getX() - radius, origin.getY() - radius, origin.getZ() - radius,
                    origin.getX() + radius, origin.getY() + radius, origin.getZ() + radius
            );

            this.audits.audit(world,
                    au -> {
                        var center = Vec3.atCenterOf(origin);
                        for (var pos : BlockPos.betweenClosed(area.min(), area.max())) {
                            if (restriction != null && !restriction.contains(pos)) {
                                continue;
                            }
                            if (!params.canSet(world, pos)) {
                                continue;
                            }

                            double lx = pos.getX() + 0.5 - center.x();
                            double ly = pos.getY() + 0.5 - center.y();
                            double lz = pos.getZ() + 0.5 - center.z();

                            double h = (lx * lx) + (ly * ly) + (lz * lz);

                            if (h > radius * radius) {
                                continue;
                            }

                            au.setBlockState(pos, params.getBlock(world));
                        }
                    }, blocksChanged);
        }
    }

    public void deleteSelectionDisplay() {
        if (this.selectionDisplay != null) {
            this.selectionDisplay.remove(Entity.RemovalReason.KILLED);
            this.selectionDisplay = null;
        }
    }

    public void updateSelectionDisplay(Level world, BlockState state) {
        if (this.selectStart == null) {
            this.deleteSelectionDisplay();
            return;
        }

        if (this.selectionDisplay == null) {
            this.selectionDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world);
            world.addFreshEntity(this.selectionDisplay);
        }

        if (this.selectionDisplay.getBlockState() != state) {
            this.selectionDisplay.setBlockState(state);
        }

        var minPos = new Vec3(
                Math.min(this.selectStart.getX(), this.selectEnd.getX()),
                Math.min(this.selectStart.getY(), this.selectEnd.getY()),
                Math.min(this.selectStart.getZ(), this.selectEnd.getZ())
        );
        this.selectionDisplay.setPos(minPos.subtract(0.05, 0.05, 0.05));

        float scaleX = 0.1f + Math.max(this.selectStart.getX(), this.selectEnd.getX()) + 1 - (float) minPos.x();
        float scaleY = 0.1f + Math.max(this.selectStart.getY(), this.selectEnd.getY()) + 1 - (float) minPos.y();
        float scaleZ = 0.1f + Math.max(this.selectStart.getZ(), this.selectEnd.getZ()) + 1 - (float) minPos.z();

        this.selectionDisplay.setTransformation(new Transformation(new Matrix4f().scale(scaleX, scaleY, scaleZ)));
        this.selectionDisplay.setBrightnessOverride(Brightness.FULL_BRIGHT);
    }

    public void tickSelecting(BlockPos cursorPos) {
        if (this.selectStart == null) {
            this.selectStart = cursorPos;
        }

        this.selectEnd.set(cursorPos);
    }

    public BlockBounds endSelection(BlockPos cursorPos) {
        tickSelecting(cursorPos);

        var bounds = BlockBounds.of(this.selectStart, this.selectEnd);
        this.selectStart = null;
        this.deleteSelectionDisplay();

        return bounds;
    }

    public void destroy() {
        if (PLAYERS.containsKey(this.player)) {
            PLAYERS.get(this.player).remove(this);
        }
    }

    public static void onServerStart() {
        PLAYERS.clear();
    }

    public static BDToolsState get(ServerPlayer player) {
        var ref = PlayerRef.of(player);
        var forPlayer = PLAYERS.computeIfAbsent(ref, p -> new ArrayDeque<>());

        BDToolsState state;
        if (forPlayer.isEmpty()) {
            state = new BDToolsState.Conditional(player.level().getServer(), ref, DEFAULT_MAX_UNDOS, null);
            forPlayer.addLast(state);
        } else {
            state = forPlayer.getLast();
        }

        return state;
    }

    public static class Conditional extends BDToolsState {
        public static final Component NOT_PERMITTED = Component.translatable("message.builderdash.tool.no_permission").withStyle(ChatFormatting.RED);

        public Conditional(MinecraftServer server, PlayerRef player, int maxUndos, @Nullable BlockBounds restriction) {
            super(server, player, maxUndos, restriction);
        }

        @Override
        public boolean denyOperation() {
            boolean allowed = false;
            var entity = player.getEntity(this.server);
            if (entity != null) {
                allowed = Permissions.check(entity, BDUtil.PERM_GLOBAL_TOOLBOX, 2);
            }

            if (allowed) {
                return false;
            }

            this.player.ifOnline(this.server, p -> p.sendSystemMessage(NOT_PERMITTED, false));
            return true;
        }
    }

    public static class Forbidden extends BDToolsState {
        public static final Component FORBIDDEN = Component.translatable("message.builderdash.tool.forbidden").withStyle(ChatFormatting.RED);

        public Forbidden(MinecraftServer server, PlayerRef player, int maxUndos, @Nullable BlockBounds restriction) {
            super(server, player, maxUndos, restriction);
        }

        @Override
        public boolean denyOperation() {
            this.player.ifOnline(this.server, p ->
                    p.sendSystemMessage(FORBIDDEN, false));

            return true;
        }

        @Override
        public void deleteSelectionDisplay() {
        }

        @Override
        public void updateSelectionDisplay(Level world, BlockState state) {
        }
    }

    public record OperationParams(Set<Block> filter, boolean blacklist, List<BlockState> paint) {
        public static OperationParams of(Inventory inv) {
            var filter = new HashSet<Block>();
            boolean blacklist = false;
            var paint = new ArrayList<BlockState>();

            for (int i = 0; i < Inventory.SELECTION_SIZE; i++) {
                var stack = inv.getItem(i);
                var data = stack.get(DataComponents.CUSTOM_DATA);

                if (data != null) {
                    var nbt = data.copyTag();
                    var bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
                    if (nbt.contains("builderdash:filter") && bundle != null) {
                        for (var fStack : bundle.items()) {
                            if (fStack.getItem() instanceof BlockItem block) {
                                filter.add(block.getBlock());
                            } else if (fStack.is(Items.GLASS_BOTTLE)) {
                                filter.add(Blocks.AIR);
                                filter.add(Blocks.CAVE_AIR);
                                filter.add(Blocks.VOID_AIR);
                            }
                        }

                        blacklist = nbt.getBoolean("builderdash:filter").orElse(false);
                        break;
                    }
                }
            }

            var offhandStack = inv.getItem(Inventory.SLOT_OFFHAND);
            var paintBundle = offhandStack.get(DataComponents.BUNDLE_CONTENTS);
            if (offhandStack.getItem() instanceof BlockItem block) {
                var stateData = offhandStack.get(DataComponents.BLOCK_STATE);
                var state = block.getBlock().defaultBlockState();
                if (stateData != null) {
                    state = stateData.apply(state);
                }
                paint.add(state);
            } else if (paintBundle != null) for (var stack : paintBundle.items()) {
                if (stack.getItem() instanceof BlockItem block) {
                    var stateData = offhandStack.get(DataComponents.BLOCK_STATE);
                    var state = block.getBlock().defaultBlockState();
                    if (stateData != null) {
                        state = stateData.apply(state);
                    }

                    for (int i = 0; i < stack.getCount(); i++) paint.add(state);
                }
            }

            return new OperationParams(filter, blacklist, paint);
        }

        public boolean canSet(Level world, BlockPos pos) {
            if (filter().isEmpty()) {
                return true;
            }

            for (var block : filter()) {
                if (blacklist()) {
                    if (world.getBlockState(pos).is(block)) {
                        return false;
                    }
                } else if (world.getBlockState(pos).is(block)) {
                    return true;
                }
            }

            return blacklist();
        }

        public BlockState getBlock(Level world) {
            if (this.paint().isEmpty()) {
                return Blocks.AIR.defaultBlockState();
            }

            return this.paint().get(world.random.nextInt(this.paint().size()));
        }
    }
}
