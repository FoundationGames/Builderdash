package io.github.foundationgames.builderdash.game.mode.versus;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.game.CustomWordsPersistentState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public enum VersusCommand {;
    public static LiteralArgumentBuilder<ServerCommandSource> createCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        command.requires(BDUtil.permission(BDVersusConfig.VERSUS, BDUtil.PERM_GAME_OPEN, 2));

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            command.then(CommandManager.literal("testmode")
                    .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDVersusConfig.TEST_CONFIG))
            );
        }

        command
                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDVersusConfig.DEFAULT_CONFIG))
                .then(CommandManager.literal("double")
                        .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDVersusConfig.DOUBLE_CONFIG))
                        .then(CommandManager.literal("fast")
                                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDVersusConfig.DOUBLE_FAST_CONFIG))
                        )
                )
                .then(CommandManager.literal("fast")
                        .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDVersusConfig.FAST_CONFIG))
                        .then(CommandManager.literal("double")
                                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDVersusConfig.DOUBLE_FAST_CONFIG))
                        )
                );
        return CustomWordsPersistentState.createCommand(command, BDVersusConfig.VERSUS);
    }

}
