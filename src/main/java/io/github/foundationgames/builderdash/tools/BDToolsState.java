package io.github.foundationgames.builderdash.tools;

import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.tools.ui.BDToolboxGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.Brightness;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
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
    public static final Text UNDO_FAIL = Text.translatable("message.builderdash.tool.undo_fail").formatted(Formatting.RED);
    public static final Text REDO_FAIL = Text.translatable("message.builderdash.tool.redo_fail").formatted(Formatting.RED);

    public static final int DEFAULT_MAX_UNDOS = 16;
    private static final Map<PlayerRef, Deque<BDToolsState>> PLAYERS = new HashMap<>();

    public final MinecraftServer server;
    public final PlayerRef player;
    public final AuditLog audits;
    public final @Nullable BlockBounds restriction;

    private BlockPos selectStart = null;
    private final BlockPos.Mutable selectEnd = new BlockPos.Mutable();

    private DisplayEntity.BlockDisplayEntity selectionDisplay = null;

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

    public void openToolbox(ServerPlayerEntity player) {
        if (denyOperation()) return;

        new BDToolboxGui(player).open();
    }

    public void undo() {
        if (denyOperation()) return;

        int[] blocksChanged = {0};
        boolean success = this.audits.undo(blocksChanged);

        var player = this.player.getEntity(this.server);
        if (player != null) {
            player.sendMessageToClient(success ? Text.translatable(UNDO, blocksChanged[0]).formatted(Formatting.AQUA)
                            : UNDO_FAIL, false);
        }
    }

    public void redo() {
        if (denyOperation()) return;

        int[] blocksChanged = {0};
        boolean success = this.audits.redo(blocksChanged);

        var player = this.player.getEntity(this.server);
        if (player != null) {
            player.sendMessageToClient(success ? Text.translatable(REDO, blocksChanged[0]).formatted(Formatting.AQUA)
                            : REDO_FAIL, false);
        }
    }

    public void fill(BlockBounds area) {
        if (denyOperation()) return;

        var player = this.player.getEntity(this.server);
        if (player != null) {
            var params = OperationParams.of(player.getInventory());
            var world = player.getServerWorld();
            int[] blocksChanged = {0};

            this.audits.audit(world,
                    au -> {
                        for (var pos : BlockPos.iterate(area.min(), area.max())) {
                            if (restriction != null && !restriction.contains(pos)) {
                                continue;
                            }
                            if (!params.canSet(world, pos)) {
                                continue;
                            }

                            au.setBlockState(pos, params.getBlock(world));
                        }
                    }, blocksChanged);

            player.sendMessageToClient(Text.translatable(OPERATION, blocksChanged[0])
                    .formatted(Formatting.LIGHT_PURPLE), false);
        }
    }

    public void sphere(BlockBounds area) {
        if (denyOperation()) return;

        var player = this.player.getEntity(this.server);
        if (player != null) {
            var params = OperationParams.of(player.getInventory());
            var world = player.getServerWorld();
            int[] blocksChanged = {0};

            double xRad = area.size().getX() * 0.5 + 0.25;
            double yRad = area.size().getY() * 0.5 + 0.25;
            double zRad = area.size().getZ() * 0.5 + 0.25;

            this.audits.audit(world,
                    au -> {
                        var center = Vec3d.ofCenter(area.min()).add(Vec3d.ofCenter(area.max())).multiply(0.5);
                        for (var pos : BlockPos.iterate(area.min(), area.max())) {
                            if (restriction != null && !restriction.contains(pos)) {
                                continue;
                            }
                            if (!params.canSet(world, pos)) {
                                continue;
                            }

                            double lx = pos.getX() + 0.5 - center.getX();
                            double ly = pos.getY() + 0.5 - center.getY();
                            double lz = pos.getZ() + 0.5 - center.getZ();

                            double h = ((lx * lx) / (xRad * xRad)) + ((ly * ly) / (yRad * yRad)) + ((lz * lz) / (zRad * zRad));

                            if (h > 1) {
                                continue;
                            }

                            au.setBlockState(pos, params.getBlock(world));
                        }
                    }, blocksChanged);

            player.sendMessageToClient(Text.translatable(OPERATION, blocksChanged[0])
                    .formatted(Formatting.LIGHT_PURPLE), false);
        }
    }

    public void cylinder(BlockBounds area) {
        if (denyOperation()) return;

        var player = this.player.getEntity(this.server);
        if (player != null) {
            var params = OperationParams.of(player.getInventory());
            var world = player.getServerWorld();
            int[] blocksChanged = {0};

            double xRad = area.size().getX() * 0.5 + 0.25;
            double zRad = area.size().getZ() * 0.5 + 0.25;

            this.audits.audit(world,
                    au -> {
                        var center = Vec3d.ofCenter(area.min()).add(Vec3d.ofCenter(area.max())).multiply(0.5);
                        for (var pos : BlockPos.iterate(area.min(), area.max())) {
                            if (restriction != null && !restriction.contains(pos)) {
                                continue;
                            }
                            if (!params.canSet(world, pos)) {
                                continue;
                            }

                            double lx = pos.getX() + 0.5 - center.getX();
                            double lz = pos.getZ() + 0.5 - center.getZ();

                            double h = ((lx * lx) / (xRad * xRad)) + ((lz * lz) / (zRad * zRad));

                            if (h > 1) {
                                continue;
                            }

                            au.setBlockState(pos, params.getBlock(world));
                        }
                    }, blocksChanged);

            player.sendMessageToClient(Text.translatable(OPERATION, blocksChanged[0])
                    .formatted(Formatting.LIGHT_PURPLE), false);
        }
    }

    public void brush(BlockPos origin, int radius) {
        if (denyOperation()) return;

        var player = this.player.getEntity(this.server);
        if (player != null) {
            var params = OperationParams.of(player.getInventory());
            var world = player.getServerWorld();
            int[] blocksChanged = {0};

            var area = BlockBounds.of(
                    origin.getX() - radius, origin.getY() - radius, origin.getZ() - radius,
                    origin.getX() + radius, origin.getY() + radius, origin.getZ() + radius
            );

            this.audits.audit(world,
                    au -> {
                        var center = Vec3d.ofCenter(origin);
                        for (var pos : BlockPos.iterate(area.min(), area.max())) {
                            if (restriction != null && !restriction.contains(pos)) {
                                continue;
                            }
                            if (!params.canSet(world, pos)) {
                                continue;
                            }

                            double lx = pos.getX() + 0.5 - center.getX();
                            double ly = pos.getY() + 0.5 - center.getY();
                            double lz = pos.getZ() + 0.5 - center.getZ();

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

    public void updateSelectionDisplay(World world, BlockState state) {
        if (this.selectStart == null) {
            this.deleteSelectionDisplay();
            return;
        }

        if (this.selectionDisplay == null) {
            this.selectionDisplay = new DisplayEntity.BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world);
            world.spawnEntity(this.selectionDisplay);
        }

        if (this.selectionDisplay.getBlockState() != state) {
            this.selectionDisplay.setBlockState(state);
        }

        var minPos = new Vec3d(
                Math.min(this.selectStart.getX(), this.selectEnd.getX()),
                Math.min(this.selectStart.getY(), this.selectEnd.getY()),
                Math.min(this.selectStart.getZ(), this.selectEnd.getZ())
        );
        this.selectionDisplay.setPosition(minPos.subtract(0.05, 0.05, 0.05));

        float scaleX = 0.1f + Math.max(this.selectStart.getX(), this.selectEnd.getX()) + 1 - (float) minPos.getX();
        float scaleY = 0.1f + Math.max(this.selectStart.getY(), this.selectEnd.getY()) + 1 - (float) minPos.getY();
        float scaleZ = 0.1f + Math.max(this.selectStart.getZ(), this.selectEnd.getZ()) + 1 - (float) minPos.getZ();

        this.selectionDisplay.setTransformation(new AffineTransformation(new Matrix4f().scale(scaleX, scaleY, scaleZ)));
        this.selectionDisplay.setBrightness(Brightness.FULL);
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

    public static BDToolsState get(ServerPlayerEntity player) {
        var ref = PlayerRef.of(player);
        var forPlayer = PLAYERS.computeIfAbsent(ref, p -> new ArrayDeque<>());

        BDToolsState state;
        if (forPlayer.isEmpty()) {
            state = new BDToolsState.Conditional(player.getServer(), ref, DEFAULT_MAX_UNDOS, null);
            forPlayer.addLast(state);
        } else {
            state = forPlayer.getLast();
        }

        return state;
    }

    public static class Conditional extends BDToolsState {
        public static final Text NOT_PERMITTED = Text.translatable("message.builderdash.tool.no_permission").formatted(Formatting.RED);

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

            this.player.ifOnline(this.server, p -> p.sendMessageToClient(NOT_PERMITTED, false));
            return true;
        }
    }

    public static class Forbidden extends BDToolsState {
        public static final Text FORBIDDEN = Text.translatable("message.builderdash.tool.forbidden").formatted(Formatting.RED);

        public Forbidden(MinecraftServer server, PlayerRef player, int maxUndos, @Nullable BlockBounds restriction) {
            super(server, player, maxUndos, restriction);
        }

        @Override
        public boolean denyOperation() {
            this.player.ifOnline(this.server, p ->
                    p.sendMessageToClient(FORBIDDEN, false));

            return true;
        }

        @Override
        public void deleteSelectionDisplay() {
        }

        @Override
        public void updateSelectionDisplay(World world, BlockState state) {
        }
    }

    public record OperationParams(Set<Block> filter, boolean blacklist, List<BlockState> paint) {
        public static OperationParams of(PlayerInventory inv) {
            var filter = new HashSet<Block>();
            boolean blacklist = false;
            var paint = new ArrayList<BlockState>();

            for (int i = 0; i < PlayerInventory.HOTBAR_SIZE; i++) {
                var stack = inv.getStack(i);
                var data = stack.get(DataComponentTypes.CUSTOM_DATA);

                if (data != null) {
                    var nbt = data.copyNbt();
                    var bundle = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
                    if (nbt.contains("builderdash:filter") && bundle != null) {
                        for (var fStack : bundle.iterate()) {
                            if (fStack.getItem() instanceof BlockItem block) {
                                filter.add(block.getBlock());
                            } else if (fStack.isOf(Items.GLASS_BOTTLE)) {
                                filter.add(Blocks.AIR);
                                filter.add(Blocks.CAVE_AIR);
                                filter.add(Blocks.VOID_AIR);
                            }
                        }

                        blacklist = nbt.getBoolean("builderdash:filter");
                        break;
                    }
                }
            }

            var offhandStack = inv.getStack(PlayerInventory.OFF_HAND_SLOT);
            var paintBundle = offhandStack.get(DataComponentTypes.BUNDLE_CONTENTS);
            if (offhandStack.getItem() instanceof BlockItem block) {
                var stateData = offhandStack.get(DataComponentTypes.BLOCK_STATE);
                var state = block.getBlock().getDefaultState();
                if (stateData != null) {
                    state = stateData.applyToState(state);
                }
                paint.add(state);
            } else if (paintBundle != null) for (var stack : paintBundle.iterate()) {
                if (stack.getItem() instanceof BlockItem block) {
                    var stateData = offhandStack.get(DataComponentTypes.BLOCK_STATE);
                    var state = block.getBlock().getDefaultState();
                    if (stateData != null) {
                        state = stateData.applyToState(state);
                    }

                    for (int i = 0; i < stack.getCount(); i++) paint.add(state);
                }
            }

            return new OperationParams(filter, blacklist, paint);
        }

        public boolean canSet(World world, BlockPos pos) {
            if (filter().isEmpty()) {
                return true;
            }

            for (var block : filter()) {
                if (blacklist()) {
                    if (world.getBlockState(pos).isOf(block)) {
                        return false;
                    }
                } else if (world.getBlockState(pos).isOf(block)) {
                    return true;
                }
            }

            return blacklist();
        }

        public BlockState getBlock(World world) {
            if (this.paint().isEmpty()) {
                return Blocks.AIR.getDefaultState();
            }

            return this.paint().get(world.random.nextInt(this.paint().size()));
        }
    }
}
