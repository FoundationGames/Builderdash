package io.github.foundationgames.builderdash.game;

import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.plasmid.api.game.GameSpace;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public class BDPlayerLogic {
    private final GameSpace gameSpace;
    private final BuilderdashMap map;
    private final ServerLevel world;

    public BDPlayerLogic(GameSpace gameSpace, ServerLevel world, BuilderdashMap map) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.world = world;
    }

    public void resetPlayer(ServerPlayer player, @Nullable BDPlayer data) {
        var mode = GameType.ADVENTURE;
        if (data != null && data.currentRole != null) {
            mode = data.currentRole.getGameMode();
        }

        resetPlayer(player, mode);
    }

    public void resetPlayer(ServerPlayer player, GameType gameMode) {
        player.setGameMode(gameMode);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0f;

        player.addEffect(new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));
    }

    public Vec3 getSpawnPos(RandomSource random, BlockBounds bounds) {
        var minPos = bounds.min();
        var maxPos = bounds.max();

        var pos = new BlockPos(
                random.nextIntBetweenInclusive(minPos.getX(), maxPos.getX()),
                minPos.getY(),
                random.nextIntBetweenInclusive(minPos.getZ(), maxPos.getZ()));

        double x = pos.getX() + Mth.nextDouble(random, -0.5, 0.5);
        double z = pos.getZ() + Mth.nextDouble(random, -0.5, 0.5);

        var mpos = new BlockPos.MutableBlockPos();
        mpos.set(pos);
        for (int i = 0; i < 72; i++) {
            if (!world.getBlockState(mpos).isSuffocating(world, mpos)) {
                mpos.move(Direction.UP);
                if (!world.getBlockState(mpos).isSuffocating(world, mpos)) {
                    break;
                }
            }

            mpos.move(Direction.UP);
        }

        return new Vec3(x, mpos.getY() + 0.1, z);
    }

    public void spawnPlayer(ServerPlayer player, BlockBounds bounds, Vec3 lookAt) {
        var pos = getSpawnPos(player.getRandom(), bounds);
        var disp = lookAt.subtract(pos.x(), 0, pos.z());

        player.teleportTo(this.world, pos.x(), pos.y(), pos.z(), Set.of(),
                (float) Math.toDegrees(Math.atan2(-disp.z(), -disp.x())) + 90, 0, true);
    }
}
