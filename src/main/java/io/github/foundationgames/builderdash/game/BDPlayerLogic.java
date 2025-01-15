package io.github.foundationgames.builderdash.game;

import io.github.foundationgames.builderdash.game.map.BuilderdashMap;
import io.github.foundationgames.builderdash.game.player.BDPlayer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

    public void spawnPlayer(ServerPlayerEntity player, BlockBounds bounds) {
        var random = player.getRandom();

        var minPos = bounds.min();
        var maxPos = bounds.max();

        var pos = new BlockPos(
                random.nextBetween(minPos.getX(), maxPos.getX()),
                minPos.getY(),
                random.nextBetween(minPos.getZ(), maxPos.getZ()));

        float x = pos.getX() + MathHelper.nextFloat(player.getRandom(), -0.5f, 0.5f);
        float z = pos.getZ() + MathHelper.nextFloat(player.getRandom(), -0.5f, 0.5f);

        player.teleport(this.world, x, pos.getY() + 1.5, z, Set.of(), 0.0F, 0.0F, true);
    }
}
