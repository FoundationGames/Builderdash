package io.github.foundationgames.builderdash.game;

import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.Set;

public class BDPlayerLogic {
    private final GameSpace gameSpace;
    private final BuilderdashMap map;
    private final ServerWorld world;

    public BDPlayerLogic(GameSpace gameSpace, ServerWorld world, BuilderdashMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.world = world;
    }

    public void resetPlayer(ServerPlayerEntity player, @Nullable BDPlayer data) {
        var mode = GameMode.ADVENTURE;
        if (data != null && data.currentRole != null) {
            mode = data.currentRole.getGameMode();
        }

        resetPlayer(player, mode);
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));
    }

    public Vec3d getSpawnPos(Random random, BlockBounds bounds) {
        var minPos = bounds.min();
        var maxPos = bounds.max();

        var pos = new BlockPos(
                random.nextBetween(minPos.getX(), maxPos.getX()),
                minPos.getY(),
                random.nextBetween(minPos.getZ(), maxPos.getZ()));

        double x = pos.getX() + MathHelper.nextDouble(random, -0.5, 0.5);
        double z = pos.getZ() + MathHelper.nextDouble(random, -0.5, 0.5);

        var mpos = new BlockPos.Mutable();
        mpos.set(pos);
        for (int i = 0; i < 72; i++) {
            if (!world.getBlockState(mpos).shouldSuffocate(world, mpos)) {
                mpos.move(Direction.UP);
                if (!world.getBlockState(mpos).shouldSuffocate(world, mpos)) {
                    break;
                }
            }

            mpos.move(Direction.UP);
        }

        return new Vec3d(x, mpos.getY() + 0.1, z);
    }

    public void spawnPlayer(ServerPlayerEntity player, BlockBounds bounds, Vec3d lookAt) {
        var pos = getSpawnPos(player.getRandom(), bounds);
        var disp = lookAt.subtract(pos.getX(), 0, pos.getZ());

        player.teleport(this.world, pos.getX(), pos.getY(), pos.getZ(), Set.of(),
                (float) Math.toDegrees(Math.atan2(-disp.getZ(), -disp.getX())) + 90, 0, true);
    }
}
