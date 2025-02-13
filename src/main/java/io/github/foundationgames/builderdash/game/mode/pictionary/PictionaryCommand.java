package io.github.foundationgames.builderdash.game.mode.pictionary;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.builderdash.BDUtil;
import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.game.CustomWordsPersistentState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public enum PictionaryCommand {;
    public static LiteralArgumentBuilder<ServerCommandSource> createCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        command.requires(BDUtil.permission(BDPictionaryConfig.PICTIONARY, BDUtil.PERM_GAME_OPEN, 2));

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            command.then(CommandManager.literal("testmode")
                    .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDPictionaryConfig.TEST_CONFIG))
            );
        }

        command
                .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDPictionaryConfig.DEFAULT_CONFIG))
                .then(CommandManager.literal("double")
                        .executes(ctx -> Builderdash.openBuilderdashGame(ctx.getSource(), BDPictionaryConfig.DOUBLE_CONFIG))
                );
        return CustomWordsPersistentState.createCommand(command, BDPictionaryConfig.PICTIONARY);
    }
}
