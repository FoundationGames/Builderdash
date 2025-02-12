package io.github.foundationgames.builderdash.game.mode.pictionary;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.foundationgames.builderdash.Builderdash;
import io.github.foundationgames.builderdash.game.CustomWordsPersistentState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public enum PictionaryCommand {;
    public static final String PICTIONARY_NAME = "name.builderdash.pictionary";

    public static LiteralArgumentBuilder<ServerCommandSource> createCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
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
        return CustomWordsPersistentState.createCommand(command, CustomWordsPersistentState.PICTIONARY_KEY, PICTIONARY_NAME);
    }
}
